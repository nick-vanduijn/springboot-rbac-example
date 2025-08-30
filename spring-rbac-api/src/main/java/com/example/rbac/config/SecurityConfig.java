package com.example.rbac.config;

import com.example.rbac.security.JwtAuthenticationFilter;
import com.example.rbac.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for real RBAC with JWT authentication.
 * Configures Spring Security for role-based access control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserService userService,
      PasswordEncoder passwordEncoder) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Configures the security filter chain for real authentication and
   * authorization.
   *
   * @param http the HttpSecurity object
   * @return SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/users/**")).hasRole("USER")
            .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("ADMIN")
            .anyRequest().authenticated())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin()));

    return http.build();
  }

  /**
   * Authentication provider for real user authentication.
   *
   * @return DaoAuthenticationProvider
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  /**
   * Authentication manager for handling authentication requests.
   *
   * @param config the authentication configuration
   * @return AuthenticationManager
   * @throws Exception if configuration fails
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}