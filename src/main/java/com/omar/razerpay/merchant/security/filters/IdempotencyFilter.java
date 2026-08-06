package com.omar.razerpay.merchant.security.filters;

import com.omar.razerpay.common.exception.IdempotencyConflictException;
import com.omar.razerpay.common.idempotency.IdempotencyStore;
import com.omar.razerpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    public static final Duration COMPLETED_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = ":";

    private final MerchantContext merchantContext;
    private final IdempotencyStore store;
    private final HandlerExceptionResolver handlerExceptionResolver;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!GUARDED_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-Idempotency-key");

        if (rawKey == null || rawKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String key = merchantId != null ? merchantId + SEPARATOR + rawKey : rawKey;

        boolean alreadyClaimed = !store.setIfAbsent(key, IN_PROGRESS_TTL);

        if (alreadyClaimed) {
            //another thread has already claimed this key
            Optional<String> existing = store.get(key);

            if (existing.isPresent() && !IdempotencyStore.IN_PROGRESS.equals(existing.get())) {
                //It's not in progress, meaning the thread that was working on it has finished
                replay(request, response, existing.get());
            } else {
                //It's in progress by another thread
                var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
            return;
        }

        //read notes at the bottom of for why we wrap the response in a ContentCachingResponseWrapper
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
        } finally {
            int status = wrapper.getStatus();
            byte[] bodyBytes = wrapper.getContentAsByteArray();

            // 1. Convert the cached bytes back into a String (JSON)
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            // 2. We only want to cache successful responses (e.g., 200 OK, 201 Created)
            // If it's a 4xx or 5xx, we might want to delete the key so they can try again.
            if (status >= 200 && status < 300) {
                String stored = status + SEPARATOR + body;
                // Save the successful response to Redis for 24 hours
                store.store(key, stored, COMPLETED_TTL);
                log.debug("IdempotencyFilter: Stored key={}, status={}, stored={}", key, status, stored);

            } else if (status >= 400) {
                // On failure, release the lock so the user can retry immediately
                store.delete(key);
                log.debug("IdempotencyFilter: Deleted key={} due to failure, status={}", key, status);
            }

            // 3. THIS IS MANDATORY!
            // This flushes the cached bytes out to the actual network stream.
            // Without this, the client receives an empty body.
            wrapper.copyBodyToResponse();

        }
    }

    private void replay(HttpServletRequest request, HttpServletResponse response, String cachedValue) throws IOException {
        log.info("IdempotencyFilter: Replaying cached response");

        // 1. Find where the status ends and the body begins
        int separatorIndex = cachedValue.indexOf(SEPARATOR);

        if (separatorIndex == -1) {
            // Fallback in case the cached data got corrupted
            log.error("IdempotencyFilter: Malformed cached value: {}", cachedValue);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Corrupted idempotency data");
            return;
        }

        try {
            // 2. Extract the status code and body
            int status = Integer.parseInt(cachedValue.substring(0, separatorIndex));
            String body = cachedValue.substring(separatorIndex + SEPARATOR.length());

            // 3. Set the HTTP status code
            response.setStatus(status);

            // 4. Set the standard headers for a JSON API response
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // (Optional but recommended) Add a header so the frontend knows this was a cached retry
            response.setHeader("X-Idempotency-Replayed", "true");

            // 5. Write the body directly to the output stream
            response.getWriter().write(body);
            response.getWriter().flush();

        } catch (Exception e) {
            log.error("IdempotencyFilter: Failed to parse response from cache", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cache parsing error");
        }
    }
}

/*
What is ContentCachingResponseWrapper?
When you wrap the response in a ContentCachingResponseWrapper, it completely hijacks the output stream.

    Standard behavior: When your request passes through the filter and reaches your Controller, your Controller writes the JSON response
    (e.g., {"status": "success", "transaction_id": 123}) into that stream. Once that data is written, it flows directly out to the network and to the client.

    The Hijack: Instead of passing standard HttpServletResponse to the controller, we wrap it in ContentCachingResponseWrapper
    The wrapper swaps out the real network OutputStream for a fake, in-memory ByteArrayOutputStream.

    The Write: When your Controller returns the JSON, it thinks it is writing to the network, but it is actually writing into that fake in-memory byte array.

    The Trap: The real network stream remains 100% empty. Nothing goes to the client.

    The Release: This is why calling wrapper.copyBodyToResponse() at the end of your finally block is mandatory. That method takes everything trapped in the fake memory array and dumps it into the real network stream so the user finally gets their data.
 */
