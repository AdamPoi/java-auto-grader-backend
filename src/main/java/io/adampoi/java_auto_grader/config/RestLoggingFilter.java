package io.adampoi.java_auto_grader.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "anonymous";

        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);

        log.info("[REST-REQ] id='{}', user='{}', ip='{}', method={}, uri='{}', query='{}'",
                requestId, user, ip, method, uri, query != null ? query : "");

        filterChain.doFilter(request, response);

        int status = response.getStatus();
        long duration = System.currentTimeMillis() - start;

        log.info("[REST-RESP] id='{}', user='{}', method={}, uri='{}', status={}, duration={}ms",
                requestId, user, method, uri, status, duration);
    }
}
