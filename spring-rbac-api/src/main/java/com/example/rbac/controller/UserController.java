package com.example.rbac.controller;

import com.example.rbac.entity.User;
import com.example.rbac.service.UserService;
import com.example.rbac.service.AuditService;
import com.example.rbac.audit.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for user operations.
 * Provides endpoints for user profile management with real RBAC authorization
 * and comprehensive audit logging.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;
  private final AuditService auditService;

  public UserController(UserService userService, AuditService auditService) {
    this.userService = userService;
    this.auditService = auditService;
  }

  /**
   * Gets the current user's profile.
   * Requires USER role authentication.
   *
   * @param httpRequest the HTTP servlet request for audit logging
   * @return user profile information
   */
  @GetMapping("/profile")
  @PreAuthorize("hasRole('USER')")
  @Auditable
  public ResponseEntity<?> getProfile(HttpServletRequest httpRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication.getName();

      // Log profile access attempt
      auditService.logEventForUser("USER_PROFILE", "PROFILE_VIEW",
          username, "User accessed their profile information");

      User user = userService.findByUsername(username);
      if (user == null) {
        // Log profile access failure
        auditService.logEvent("USER_PROFILE", "PROFILE_VIEW_FAILURE",
            "Profile access failed for user: " + username + " - user not found", "FAILURE");

        Map<String, String> error = new HashMap<>();
        error.put("error", "User not found");
        return ResponseEntity.notFound().build();
      }

      Map<String, Object> profile = new HashMap<>();
      profile.put("username", user.getUsername());
      profile.put("email", user.getEmail());
      profile.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
      profile.put("enabled", user.isEnabled());
      profile.put("createdAt", user.getCreatedAt());

      // Log successful profile access
      auditService.logEventForUser("USER_PROFILE", "PROFILE_VIEW_SUCCESS",
          username, "Profile information retrieved successfully");

      return ResponseEntity.ok(profile);

    } catch (Exception e) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication != null ? authentication.getName() : "unknown";

      // Log profile access error
      auditService.logEvent("USER_PROFILE", "PROFILE_VIEW_ERROR",
          "Profile access error for user: " + username + ". Error: " + e.getMessage(), "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to get profile: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }

  /**
   * Updates the current user's profile.
   * Requires USER role authentication.
   *
   * @param updates     the profile updates
   * @param httpRequest the HTTP servlet request for audit logging
   * @return updated profile information
   */
  @PutMapping("/profile")
  @PreAuthorize("hasRole('USER')")
  @Auditable
  public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates,
      HttpServletRequest httpRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication.getName();

      // Log profile update attempt
      auditService.logEventForUser("USER_PROFILE", "PROFILE_UPDATE",
          username, "User attempted to update profile. Fields: " + updates.keySet());

      User user = userService.findByUsername(username);
      if (user == null) {
        // Log profile update failure
        auditService.logEvent("USER_PROFILE", "PROFILE_UPDATE_FAILURE",
            "Profile update failed for user: " + username + " - user not found", "FAILURE");

        Map<String, String> error = new HashMap<>();
        error.put("error", "User not found");
        return ResponseEntity.notFound().build();
      }

      // Track what was changed for audit
      StringBuilder changeDetails = new StringBuilder("Profile updated - Changes: ");

      // Update allowed fields
      if (updates.containsKey("email")) {
        String oldEmail = user.getEmail();
        String newEmail = (String) updates.get("email");
        user.setEmail(newEmail);
        changeDetails.append("email changed from ").append(oldEmail).append(" to ").append(newEmail).append("; ");
      }

      userService.updateUser(user);

      Map<String, Object> profile = new HashMap<>();
      profile.put("username", user.getUsername());
      profile.put("email", user.getEmail());
      profile.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
      profile.put("enabled", user.isEnabled());
      profile.put("updatedAt", user.getUpdatedAt());

      // Log successful profile update
      auditService.logEventForUser("USER_PROFILE", "PROFILE_UPDATE_SUCCESS",
          username, changeDetails.toString());

      return ResponseEntity.ok(profile);

    } catch (Exception e) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication != null ? authentication.getName() : "unknown";

      // Log profile update error
      auditService.logEvent("USER_PROFILE", "PROFILE_UPDATE_ERROR",
          "Profile update error for user: " + username + ". Error: " + e.getMessage(), "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to update profile: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }
}