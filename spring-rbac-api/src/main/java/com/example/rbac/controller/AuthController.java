package com.example.rbac.controller;

import com.example.rbac.dto.LoginRequest;
import com.example.rbac.dto.LoginResponse;
import com.example.rbac.dto.RegisterRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.service.JwtService;
import com.example.rbac.service.UserService;
import com.example.rbac.service.AuditService;
import com.example.rbac.audit.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration and login with real JWT
 * authentication and comprehensive audit logging.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final AuditService auditService;

  public AuthController(UserService userService, JwtService jwtService,
      AuthenticationManager authenticationManager, AuditService auditService) {
    this.userService = userService;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.auditService = auditService;
  }

  /**
   * Registers a new user with real password encryption and role assignment.
   *
   * @param registerRequest the registration request
   * @param httpRequest     the HTTP servlet request for audit logging
   * @return success message or error
   */
  @PostMapping("/register")
  @Auditable
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest,
      HttpServletRequest httpRequest) {
    try {
      // Log registration attempt
      auditService.logEvent("USER_REGISTRATION", "REGISTER_ATTEMPT",
          "User registration attempt for username: " + registerRequest.getUsername() +
              ", email: " + registerRequest.getEmail());

      // Check if username already exists
      if (userService.usernameExists(registerRequest.getUsername())) {
        // Log failed registration (username exists)
        auditService.logEvent("USER_REGISTRATION", "REGISTER_FAILURE",
            "Registration failed for username: " + registerRequest.getUsername() +
                ". Reason: Username already exists",
            "FAILURE");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Username already exists");
        return ResponseEntity.badRequest().body(error);
      }

      // Create new user
      User user = new User(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());

      // Assign default USER role (real role-based access control)
      Role userRole = new Role("USER", "Regular user role");
      user.setRoles(Set.of(userRole));

      // Create user with encrypted password
      userService.createUser(user);

      // Log successful registration
      auditService.logEventForUser("USER_REGISTRATION", "REGISTER_SUCCESS",
          registerRequest.getUsername(), "User registration completed successfully");

      Map<String, String> response = new HashMap<>();
      response.put("message", "User registered successfully");
      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (Exception e) {
      // Log registration error
      auditService.logEvent("USER_REGISTRATION", "REGISTER_ERROR",
          "Registration error for username: " + registerRequest.getUsername() +
              ". Error: " + e.getMessage(),
          "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Registration failed: " + e.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }

  /**
   * Authenticates user and returns JWT token with real authentication.
   *
   * @param loginRequest the login request
   * @param httpRequest  the HTTP servlet request for audit logging
   * @return JWT token and user information
   */
  @PostMapping("/login")
  @Auditable
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
      HttpServletRequest httpRequest) {
    try {
      // Log login attempt
      auditService.logEvent("USER_AUTHENTICATION", "LOGIN_ATTEMPT",
          "Login attempt for username: " + loginRequest.getUsername());

      // Real authentication using Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsername(),
              loginRequest.getPassword()));

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      User user = userService.findByUsername(userDetails.getUsername());

      // Generate real JWT token
      String jwt = jwtService.generateToken(userDetails);

      // Extract roles for response
      List<String> roles = user.getRoles().stream()
          .map(Role::getName)
          .collect(Collectors.toList());

      // Create response with token and user information
      LoginResponse response = new LoginResponse(
          jwt,
          user.getUsername(),
          user.getEmail(),
          roles,
          LocalDateTime.now().plusDays(1) // Token expires in 24 hours
      );

      // Log successful login
      auditService.logEventForUser("USER_AUTHENTICATION", "LOGIN_SUCCESS",
          loginRequest.getUsername(), "User login completed successfully. Roles: " + roles);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      // Log failed login
      auditService.logEvent("USER_AUTHENTICATION", "LOGIN_FAILURE",
          "Login failed for username: " + loginRequest.getUsername() +
              ". Reason: Invalid credentials",
          "FAILURE");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Invalid credentials");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
  }
}