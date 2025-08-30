package com.example.rbac.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for login responses.
 */
public class LoginResponse {

  private String token;
  private String type = "Bearer";
  private String username;
  private String email;
  private List<String> roles;
  private LocalDateTime issuedAt;
  private LocalDateTime expiresAt;

  /**
   * Default constructor.
   */
  public LoginResponse() {
    this.issuedAt = LocalDateTime.now();
  }

  /**
   * Constructor with token and user details.
   *
   * @param token    the JWT token
   * @param username the username
   * @param email    the email
   * @param roles    the user roles
   */
  public LoginResponse(String token, String username, String email, List<String> roles) {
    this();
    this.token = token;
    this.username = username;
    this.email = email;
    this.roles = roles;
  }

  /**
   * Constructor with token, user details, and expiration.
   *
   * @param token     the JWT token
   * @param username  the username
   * @param email     the email
   * @param roles     the user roles
   * @param expiresAt when the token expires
   */
  public LoginResponse(String token, String username, String email, List<String> roles, LocalDateTime expiresAt) {
    this(token, username, email, roles);
    this.expiresAt = expiresAt;
  }

  // Getters and Setters

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public LocalDateTime getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(LocalDateTime issuedAt) {
    this.issuedAt = issuedAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public String toString() {
    return "LoginResponse{" +
        "type='" + type + '\'' +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", roles=" + roles +
        ", issuedAt=" + issuedAt +
        ", expiresAt=" + expiresAt +
        '}';
  }
}