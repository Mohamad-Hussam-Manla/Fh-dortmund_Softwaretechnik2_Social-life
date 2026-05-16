package de.fhdortmund.mystudyapp.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // Only rate limit auth endpoints
        if (!path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> new Bucket());

        synchronized (bucket) {
            bucket.clean();
            if (bucket.count() >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for {}", clientKey);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return;
            }
            bucket.add();
        }

        filterChain.doFilter(request, response);
    }

    private String getClientKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip + ":" + request.getRequestURI();
    }

    private static class Bucket {
        private final java.util.LinkedList<Long> timestamps = new java.util.LinkedList<>();

        void add() {
            timestamps.addLast(System.currentTimeMillis());
        }

        int count() {
            return timestamps.size();
        }

        void clean() {
            long now = System.currentTimeMillis();
            timestamps.removeIf(ts -> now - ts > WINDOW_MS);
        }
    }
}