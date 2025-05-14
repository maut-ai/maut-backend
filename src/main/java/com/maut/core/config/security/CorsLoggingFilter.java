package com.maut.core.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this filter runs very early
public class CorsLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CorsLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (origin != null) {
            logger.info("[CorsLoggingFilter] Incoming request: Method={}, URI={}, Origin={}", method, uri, origin);
        } else {
            logger.info("[CorsLoggingFilter] Incoming request: Method={}, URI={}, No Origin header present.", method, uri);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // This block executes after the rest of the filter chain, including Spring Security's CorsFilter
            // and your controllers/handlers.
            if (response.getStatus() == HttpStatus.FORBIDDEN.value() && origin != null) {
                logger.warn("[CorsLoggingFilter] Request from Origin {} to URI {} with Method {} resulted in a 403 FORBIDDEN response. Possible CORS issue or other access denial.", origin, uri, method);
            } else if (response.getStatus() == HttpStatus.FORBIDDEN.value()) {
                logger.warn("[CorsLoggingFilter] Request to URI {} with Method {} (no Origin header) resulted in a 403 FORBIDDEN response.", uri, method);
            }
        }
    }
}
