package de.fhdortmund.mystudyapp.common.security;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int AUTH_MAX_REQUESTS = 5;
    private static final int WRITE_MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private static final Set<String> WRITE_PATHS = Set.of(
            "/api/reviews",
            "/api/reports",
            "/api/rsvps",
            "/api/events"
    );

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only rate limit auth and write endpoints
        if (!path.startsWith("/api/auth/") && !isWritePath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);
        int maxRequests = path.startsWith("/api/auth/") ? AUTH_MAX_REQUESTS : WRITE_MAX_REQUESTS;
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> new Bucket(maxRequests));

        synchronized (bucket) {
            bucket.clean();
            if (bucket.count() >= bucket.getMaxRequests()) {
                log.warn("Rate limit exceeded for {} on {}", clientKey, path);
                
                // FIXED: Return consistent ApiResponse<Void> envelope instead of raw JSON
                ApiResponse<Void> errorResponse = ApiResponse.error("Too many requests. Please try again later.");
                String jsonBody = objectMapper.writeValueAsString(errorResponse);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(jsonBody);
                return;
            }
            bucket.add();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWritePath(String path) {
        // Check if path starts with any of the write prefixes (POST/PUT/PATCH/DELETE only)
        return WRITE_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to state-changing methods on write paths
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        return false;
    }

    private String getClientKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip + ":" + request.getRequestURI();
    }

    private static class Bucket {
        private final int maxRequests;
        private final java.util.LinkedList<Long> timestamps = new java.util.LinkedList<>();

        Bucket(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        int getMaxRequests() {
            return maxRequests;
        }

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