package com.dxc.iotmonitor.security;

import com.dxc.iotmonitor.config.RateLimitService;
import com.dxc.iotmonitor.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        String endpoint = null;
        if (path.equals("/api/auth/register")) endpoint = "register";
        else if (path.equals("/api/auth/login")) endpoint = "login";

        if(endpoint != null && !rateLimitService.tryConsume(endpoint, ip)){
            ErrorResponse error = new ErrorResponse(429, "Too Many Requests", "Too many requests. Please try again later.");
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(error)); //convert java object to its json string representation
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
