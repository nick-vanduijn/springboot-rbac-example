package com.example.rbac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 */
public class RegisterRequest {

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  /**
   * Default constructor.
   */
  public RegisterRequest() {
  }

  /**
   * Constructor with all fields.
   *
   * @param username the username
   * @param email    the email
   * @param password the password
   */
  public RegisterRequest(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // Getters and Setters

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "RegisterRequest{" +
        "username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}