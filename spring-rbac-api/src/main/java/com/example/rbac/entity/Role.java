package com.example.rbac.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Role entity representing user roles in the RBAC system.
 * Each role has a name and description.
 */
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Role name cannot be blank")
  @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
  @Column(name = "name", unique = true, nullable = false)
  private String name;

  @Size(max = 255, message = "Description cannot exceed 255 characters")
  @Column(name = "description")
  private String description;

  /**
   * Default constructor for JPA.
   */
  public Role() {
  }

  /**
   * Constructor with name and description.
   *
   * @param name        the role name
   * @param description the role description
   */
  public Role(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Constructor with name only.
   *
   * @param name the role name
   */
  public Role(String name) {
    this.name = name;
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  // equals, hashCode, and toString

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Role role = (Role) o;
    return Objects.equals(name, role.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Role{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}