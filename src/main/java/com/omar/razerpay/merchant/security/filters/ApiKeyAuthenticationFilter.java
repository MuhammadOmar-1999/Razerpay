package com.omar.razerpay.merchant.security.filters;

import com.omar.razerpay.merchant.entity.ApiKey;
import com.omar.razerpay.merchant.repository.ApiKeyRepository;
import com.omar.razerpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
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

            ApiKey apikey = apiKeyRepository.findByKeyIdAndEnabledTrue(keyId)
                    .orElseThrow(() -> new BadCredentialsException("Invalid API key"));

            if (!isValidApiKey(secret, apikey)) {
                throw new BadCredentialsException("Invalid API key");
            }

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE")));

            SecurityContextHolder.getContext().setAuthentication(auth);

            merchantContext.setMerchantId(apikey.getMerchant().getId());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private boolean isValidApiKey(String rawSecret, ApiKey apiKey) {
        if(!passwordEncoder.matches(rawSecret, apiKey.getKeySecretHash())){
            if(passwordEncoder.matches(rawSecret, apiKey.getPreviousKeySecretHash())){
                return apiKey.getGracePeriodExpiresAt() != null && apiKey.getGracePeriodExpiresAt().isAfter(LocalDateTime.now());
            }
            return false;
        }
        return true;
    }

    private String[] decodeCredentials(String header){
        String encodedCredentials = header.substring(BASIC_PREFIX.length());
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);

        int colon = decodedCredentials.indexOf(':');
        if (colon == -1) return null;

        return new String[]{decodedCredentials.substring(0, colon), decodedCredentials.substring(colon + 1)};
    }
}
