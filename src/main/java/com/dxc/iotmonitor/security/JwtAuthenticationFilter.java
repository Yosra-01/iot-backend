package com.dxc.iotmonitor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this filter runs exactly once per HTTP request

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // programming to an interface principle


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // reads the Authorization header from the incoming request
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass the request to the next filter
            return;
        }

        //removing the "Bearer " prefix
        String token = authHeader.substring(7);

        if(jwtUtil.isTokenValid(token)){
            // extracts the email from the token
            String email = jwtUtil.extractEmail(token);
            // only proceed if email was extracted and the user is not already authenticated -> prevents processing the token twice
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // loads the user details from the database using the email
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // creates an authentication token with the user's details and authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // credentials are null because we already verified the JWT
                                userDetails.getAuthorities() // roles/permissions of the user
                        );

                // attaches additional request details (e.g. IP address) to the authentication
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // sets the authenticated user in the security context for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }



        // passes the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}