package com.wonwire.wonwire.security;

import com.wonwire.wonwire.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    /**
     * Max requests allowed per endpoint within the time window.
     */
    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/auth/login", 5,
            "/api/auth/register", 3,
            "/api/auth/forgot-password", 5,
            "/api/auth/change-password", 5
    );

    private static final long WINDOW_SECONDS = 30;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equals(method) || !LIMITS.containsKey(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String key = "ratelimit:" + path + ":" + ip;

        long count = redisService.increment(key);

        // Set expiry only on first request to start the window
        if (count == 1) {
            redisService.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        int limit = LIMITS.get(path);
        if (count > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\": \"Too many requests. Please try again later.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the real client IP, accounting for reverse proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}