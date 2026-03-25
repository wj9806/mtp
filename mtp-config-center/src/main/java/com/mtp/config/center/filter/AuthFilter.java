package com.mtp.config.center.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final String[] EXCLUDE_PATHS = {
        "/api/auth/login",
        "/api/auth/logout",
        "/login",
        "/error"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        for (String exclude : EXCLUDE_PATHS) {
            if (path.startsWith(exclude)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (path.startsWith("/api/")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendUnauthorizedResponse(response, "未登录或登录已过期");
                return;
            }

            String token = authHeader.substring(7);
            if (!JwtUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "Token无效或已过期");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("code", 401);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}