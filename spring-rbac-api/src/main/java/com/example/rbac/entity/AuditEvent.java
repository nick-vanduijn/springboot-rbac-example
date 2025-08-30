package com.example.rbac.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Audit event entity for tracking user actions and system events.
 * Provides comprehensive audit trail for security and compliance.
 */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Event type cannot be blank")
  @Size(max = 50, message = "Event type must not exceed 50 characters")
  @Column(name = "event_type", nullable = false)
  private String eventType;

  @NotBlank(message = "Action cannot be blank")
  @Size(max = 100, message = "Action must not exceed 100 characters")
  @Column(name = "action", nullable = false)
  private String action;

  @Size(max = 100, message = "Username must not exceed 100 characters")
  @Column(name = "username")
  private String username;

  @Size(max = 500, message = "Details must not exceed 500 characters")
  @Column(name = "details", length = 500)
  private String details;

  @Size(max = 45, message = "IP address must not exceed 45 characters")
  @Column(name = "ip_address")
  private String ipAddress;

  @Size(max = 500, message = "User agent must not exceed 500 characters")
  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @NotNull(message = "Timestamp cannot be null")
  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Size(max = 20, message = "Status must not exceed 20 characters")
  @Column(name = "status")
  private String status; // SUCCESS, FAILURE, ERROR

  @Column(name = "resource_id")
  private Long resourceId;

  @Size(max = 50, message = "Resource type must not exceed 50 characters")
  @Column(name = "resource_type")
  private String resourceType;

  /**
   * Default constructor for JPA.
   */
  public AuditEvent() {
    this.timestamp = LocalDateTime.now();
  }

  /**
   * Constructor with essential audit information.
   *
   * @param eventType the type of event
   * @param action    the action performed
   * @param username  the username who performed the action
   */
  public AuditEvent(String eventType, String action, String username) {
    this();
    this.eventType = eventType;
    this.action = action;
    this.username = username;
    this.status = "SUCCESS";
  }

  /**
   * Constructor with full audit information.
   *
   * @param eventType the type of event
   * @param action    the action performed
   * @param username  the username who performed the action
   * @param details   additional details about the event
   * @param ipAddress the IP address of the user
   */
  public AuditEvent(String eventType, String action, String username, String details, String ipAddress) {
    this(eventType, action, username);
    this.details = details;
    this.ipAddress = ipAddress;
  }

  @PrePersist
  protected void onCreate() {
    if (this.timestamp == null) {
      this.timestamp = LocalDateTime.now();
    }
    if (this.status == null) {
      this.status = "SUCCESS";
    }
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(Long resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  // equals, hashCode, and toString

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AuditEvent that = (AuditEvent) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(eventType, that.eventType) &&
        Objects.equals(action, that.action) &&
        Objects.equals(username, that.username) &&
        Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, eventType, action, username, timestamp);
  }

  @Override
  public String toString() {
    return "AuditEvent{" +
        "id=" + id +
        ", eventType='" + eventType + '\'' +
        ", action='" + action + '\'' +
        ", username='" + username + '\'' +
        ", timestamp=" + timestamp +
        ", status='" + status + '\'' +
        '}';
  }
}