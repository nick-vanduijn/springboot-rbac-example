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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for admin operations.
 * Provides endpoints for administrative tasks with real RBAC authorization
 * and comprehensive audit logging.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final UserService userService;
  private final AuditService auditService;

  public AdminController(UserService userService, AuditService auditService) {
    this.userService = userService;
    this.auditService = auditService;
  }

  /**
   * Gets all users in the system.
   * Requires ADMIN role authentication.
   *
   * @param httpRequest the HTTP servlet request for audit logging
   * @return list of all users
   */
  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  @Auditable
  public ResponseEntity<?> getAllUsers(HttpServletRequest httpRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication.getName();

      // Log admin accessing user list
      auditService.logEventForUser("ADMIN_OPERATION", "VIEW_ALL_USERS",
          adminUsername, "Admin accessed list of all users");

      List<User> users = userService.getAllUsers();

      List<Map<String, Object>> userList = users.stream()
          .map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
            userMap.put("enabled", user.isEnabled());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
          })
          .collect(Collectors.toList());

      Map<String, Object> response = new HashMap<>();
      response.put("users", userList);
      response.put("total", userList.size());

      // Log successful user list retrieval
      auditService.logEventForUser("ADMIN_OPERATION", "VIEW_ALL_USERS_SUCCESS",
          adminUsername, "Successfully retrieved " + userList.size() + " users");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication != null ? authentication.getName() : "unknown";

      // Log error in user list retrieval
      auditService.logEvent("ADMIN_OPERATION", "VIEW_ALL_USERS_ERROR",
          "Error retrieving user list by admin: " + adminUsername + ". Error: " + e.getMessage(), "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to get users: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }

  /**
   * Deletes a user by username.
   * Requires ADMIN role authentication.
   *
   * @param username    the username to delete
   * @param httpRequest the HTTP servlet request for audit logging
   * @return success or error message
   */
  @DeleteMapping("/users/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  @Auditable
  public ResponseEntity<?> deleteUser(@PathVariable String username, HttpServletRequest httpRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication.getName();

      // Log user deletion attempt
      auditService.logEventForUser("ADMIN_OPERATION", "DELETE_USER_ATTEMPT",
          adminUsername, "Admin attempted to delete user: " + username);

      boolean deleted = userService.deleteUser(username);

      Map<String, Object> response = new HashMap<>();
      if (deleted) {
        // Log successful user deletion
        auditService.logEventForUser("ADMIN_OPERATION", "DELETE_USER_SUCCESS",
            adminUsername, "Successfully deleted user: " + username);

        response.put("message", "User deleted successfully");
        response.put("username", username);
        return ResponseEntity.ok(response);
      } else {
        // Log user deletion failure (user not found)
        auditService.logEventForUser("ADMIN_OPERATION", "DELETE_USER_FAILURE",
            adminUsername, "Failed to delete user: " + username + " (user not found)");

        response.put("error", "User not found");
        return ResponseEntity.notFound().build();
      }

    } catch (Exception e) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication != null ? authentication.getName() : "unknown";

      // Log user deletion error
      auditService.logEvent("ADMIN_OPERATION", "DELETE_USER_ERROR",
          "Error deleting user " + username + " by admin: " + adminUsername + ". Error: " + e.getMessage(), "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to delete user: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }

  /**
   * Gets system statistics.
   * Requires ADMIN role authentication.
   *
   * @param httpRequest the HTTP servlet request for audit logging
   * @return system statistics
   */
  @GetMapping("/stats")
  @PreAuthorize("hasRole('ADMIN')")
  @Auditable
  public ResponseEntity<?> getSystemStats(HttpServletRequest httpRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication.getName();

      // Log system stats access
      auditService.logEventForUser("ADMIN_OPERATION", "VIEW_SYSTEM_STATS",
          adminUsername, "Admin accessed system statistics");

      List<User> users = userService.getAllUsers();

      Map<String, Object> stats = new HashMap<>();
      stats.put("totalUsers", users.size());
      stats.put("enabledUsers", users.stream().mapToInt(user -> user.isEnabled() ? 1 : 0).sum());
      stats.put("disabledUsers", users.stream().mapToInt(user -> !user.isEnabled() ? 1 : 0).sum());

      // Log successful stats retrieval
      auditService.logEventForUser("ADMIN_OPERATION", "VIEW_SYSTEM_STATS_SUCCESS",
          adminUsername, "Successfully retrieved system statistics. Total users: " + users.size());

      return ResponseEntity.ok(stats);

    } catch (Exception e) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String adminUsername = authentication != null ? authentication.getName() : "unknown";

      // Log system stats error
      auditService.logEvent("ADMIN_OPERATION", "VIEW_SYSTEM_STATS_ERROR",
          "Error retrieving system stats by admin: " + adminUsername + ". Error: " + e.getMessage(), "ERROR");

      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to get stats: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }
}