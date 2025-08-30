package com.example.rbac.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user login requests.
 */
public class LoginRequest {

  @NotBlank(message = "Username cannot be blank")
  private String username;

  @NotBlank(message = "Password cannot be blank")
  private String password;

  /**
   * Default constructor.
   */
  public LoginRequest() {
  }

  /**
   * Constructor with username and password.
   *
   * @param username the username
   * @param password the password
   */
  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  // Getters and Setters

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "LoginRequest{" +
        "username='" + username + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}