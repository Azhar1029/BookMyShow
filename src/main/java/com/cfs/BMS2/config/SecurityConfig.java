package com.cfs.BMS2.config;

import com.cfs.BMS2.filter.JwtAuthenticationFilter;
import com.cfs.BMS2.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter,
                          CustomUserDetailsService customUserDetailsService,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                // Without this, requests with no/invalid/expired token and requests from a
                // valid but under-permissioned user both come back as 403, which makes it
                // impossible for the frontend to tell "you're not logged in" apart from
                // "you're logged in but not allowed to do this". This restores the normal
                // REST convention: 401 = not authenticated, 403 = authenticated but forbidden.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        // Every other /api/users/** GET exposes other people's name/email/phone -
                        // admin only. (register above is POST, so it isn't caught by this.)
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        // Browsing is anonymous (booking/payment still require a JWT below)
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/theaters/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/shows/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screens/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/theaters/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/theaters/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/theaters/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/screens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/screens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/screens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(
                        s ->
                                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
}