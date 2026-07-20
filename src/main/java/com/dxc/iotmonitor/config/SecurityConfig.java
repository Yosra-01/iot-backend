package com.dxc.iotmonitor.config;

import com.dxc.iotmonitor.security.CustomAuthenticationEntryPoint;
import com.dxc.iotmonitor.security.JwtAuthenticationFilter;
import com.dxc.iotmonitor.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is safely disabled: this API uses stateless JWT auth via the Authorization
                // header, not cookie-based sessions, so the browser-auto-attaches-cookies attack
                // vector that CSRF protects against does not apply here.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions — JWT handles auth state
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/user/delete",
                                "/api/sensors/generate",
                                "/api/sensors/traffic/flush",
                                "/api/sensors/air-pollution/flush",
                                "/api/sensors/street-lights/flush",
                                "/api/settings/flush",
                                "/api/alerts/flush",
                                "/api/intervals/flush"
                        ).permitAll()
                        // Scheduler / external simulators POST readings without JWT (same idea as /generate).
                        .requestMatchers(HttpMethod.POST,
                                "/api/sensors/traffic",
                                "/api/sensors/air-pollution",
                                "/api/sensors/street-lights"
                        ).permitAll()
                        //Kubernetes liveness/readiness probes hit these unauthenticated
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\""
                                            + accessDeniedException.getMessage() + "\"}"
                            );
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // exposes AuthenticationManager for login flow
    }

}
