package com.omar.razorpay.merchant.security.filters;

import com.omar.razorpay.common.exception.RateLimitException;
import com.omar.razorpay.common.ratelimit.RateLimitResult;
import com.omar.razorpay.common.ratelimit.RateLimiter;
import com.omar.razorpay.merchant.cache.ApiKeyCache;
import com.omar.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.omar.razorpay.merchant.entity.ApiKey;
import com.omar.razorpay.merchant.repository.ApiKeyRepository;
import com.omar.razorpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-limit}")
    private Integer requestsLimit;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming request: {}", request.getRequestURI());

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decodeCredentials(header);

            if(credentials == null){
                throw new BadCredentialsException("Malformed API key Header");
            }

            String keyId = credentials[0];
            String secret = credentials[1];

            ApiKeyCacheEntry cachedApiKey = apiKeyCache.get(keyId).orElseGet(() -> loadAndCache(keyId));

            if(cachedApiKey == null){
                throw new BadCredentialsException("Invalid API key");
            }

            if (!isValidApiKey(secret, cachedApiKey)) {
                throw new BadCredentialsException("Invalid API key");
            }

            RateLimitResult rateLimitResult = rateLimiter.check(
                    "api-key" + keyId,
                    requestsLimit,
                    60
            );

            if(!rateLimitResult.isAllowed()){
                log.warn("Too many requests keyId={}", keyId);
                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
            }

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE")));

            SecurityContextHolder.getContext().setAuthentication(auth);

            merchantContext.setMerchantId(cachedApiKey.merchantId());
            merchantContext.setKeyId(cachedApiKey.keyId());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyIdAndEnabledTrue(keyId)
                .orElse(null);

        ApiKeyCacheEntry cacheEntry = null;

        if(apiKey != null){
            cacheEntry = new ApiKeyCacheEntry(
                    apiKey.getKeyId(),
                    apiKey.getKeySecretHash(),
                    apiKey.getPreviousKeySecretHash(),
                    apiKey.getGracePeriodExpiresAt(),
                    apiKey.getMerchant().getId(),
                    apiKey.getEnvironment(),
                    apiKey.isEnabled()
            );
            apiKeyCache.put(keyId, cacheEntry);
        }

        return cacheEntry;
    }

    private boolean isValidApiKey(String rawSecret, ApiKeyCacheEntry cachedApiKey) {
        if(passwordEncoder.matches(rawSecret, cachedApiKey.keySecretHash())){
            return true;
        }
        return cachedApiKey.isInGracePeriod()
                && cachedApiKey.gracePeriodExpiresAt() != null
                && passwordEncoder.matches(rawSecret, cachedApiKey.previousKeySecretHash());
    }

    private String[] decodeCredentials(String header){
        String encodedCredentials = header.substring(BASIC_PREFIX.length());
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);

        int colon = decodedCredentials.indexOf(':');
        if (colon == -1) return null;

        return new String[]{decodedCredentials.substring(0, colon), decodedCredentials.substring(colon + 1)};
    }
}
