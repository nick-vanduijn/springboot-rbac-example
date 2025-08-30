package com.example.rbac.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * User entity representing users in the RBAC system.
 * Users have roles that determine their access permissions.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Column(name = "username", unique = true, nullable = false)
  private String username;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Column(name = "account_non_expired", nullable = false)
  private boolean accountNonExpired = true;

  @Column(name = "account_non_locked", nullable = false)
  private boolean accountNonLocked = true;

  @Column(name = "credentials_non_expired", nullable = false)
  private boolean credentialsNonExpired = true;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  /**
   * Default constructor for JPA.
   */
  public User() {
    this.createdAt = LocalDateTime.now();
  }

  /**
   * Constructor with username, email, and password.
   *
   * @param username the username
   * @param email    the email address
   * @param password the password
   */
  public User(String username, String email, String password) {
    this();
    this.username = username;
    this.email = email;
    this.password = password;
  }

  /**
   * Adds a role to this user.
   *
   * @param role the role to add
   */
  public void addRole(Role role) {
    this.roles.add(role);
  }

  /**
   * Removes a role from this user.
   *
   * @param role the role to remove
   */
  public void removeRole(Role role) {
    this.roles.remove(role);
  }

  /**
   * Checks if this user has a specific role.
   *
   * @param roleName the name of the role to check
   * @return true if the user has the role, false otherwise
   */
  public boolean hasRole(String roleName) {
    return roles.stream()
        .anyMatch(role -> role.getName().equals(roleName));
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  public void setAccountNonExpired(boolean accountNonExpired) {
    this.accountNonExpired = accountNonExpired;
  }

  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  public void setAccountNonLocked(boolean accountNonLocked) {
    this.accountNonLocked = accountNonLocked;
  }

  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  public void setCredentialsNonExpired(boolean credentialsNonExpired) {
    this.credentialsNonExpired = credentialsNonExpired;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  // equals, hashCode, and toString

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    User user = (User) o;
    return Objects.equals(username, user.username) && Objects.equals(email, user.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, email);
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", enabled=" + enabled +
        ", createdAt=" + createdAt +
        '}';
  }
}