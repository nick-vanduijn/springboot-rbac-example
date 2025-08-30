package com.example.rbac.security;

import com.example.rbac.service.JwtService;
import com.example.rbac.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for real token-based authentication.
 * Processes JWT tokens and sets up Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userService;

  public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // Check if Authorization header exists and starts with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract JWT token from header
    jwt = authHeader.substring(7);

    try {
      // Extract username from JWT token
      username = jwtService.extractUsername(jwt);

      // Check if username exists and user is not already authenticated
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // Load user details
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Validate token with real JWT verification
        if (jwtService.isTokenValid(jwt, userDetails)) {

          // Create authentication token
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

          // Set authentication details
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in security context
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      // Invalid token - continue without authentication
      logger.debug("JWT token validation failed: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}