package com.example.rbac.controller;

import com.example.rbac.entity.AuditEvent;
import com.example.rbac.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for audit log management.
 * Provides endpoints for administrators to view and analyze audit logs.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

  private final AuditService auditService;

  public AuditController(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Get all audit events with pagination.
   *
   * @param pageable pagination parameters
   * @return paginated list of audit events
   */
  @GetMapping
  public ResponseEntity<Page<AuditEvent>> getAllAuditEvents(
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<AuditEvent> auditEvents = auditService.getAuditEvents(pageable);
    return ResponseEntity.ok(auditEvents);
  }

  /**
   * Get audit events for a specific user.
   *
   * @param username the username to filter by
   * @param pageable pagination parameters
   * @return paginated list of audit events for the user
   */
  @GetMapping("/user/{username}")
  public ResponseEntity<Page<AuditEvent>> getAuditEventsByUser(
      @PathVariable String username,
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<AuditEvent> auditEvents = auditService.getAuditEventsByUser(username, pageable);
    return ResponseEntity.ok(auditEvents);
  }

  /**
   * Get audit events by event type.
   *
   * @param eventType the event type to filter by
   * @param pageable  pagination parameters
   * @return paginated list of audit events of the specified type
   */
  @GetMapping("/type/{eventType}")
  public ResponseEntity<Page<AuditEvent>> getAuditEventsByType(
      @PathVariable String eventType,
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<AuditEvent> auditEvents = auditService.getAuditEventsByType(eventType, pageable);
    return ResponseEntity.ok(auditEvents);
  }

  /**
   * Get audit events within a date range.
   *
   * @param startDate the start date (format: yyyy-MM-dd'T'HH:mm:ss)
   * @param endDate   the end date (format: yyyy-MM-dd'T'HH:mm:ss)
   * @param pageable  pagination parameters
   * @return paginated list of audit events within the date range
   */
  @GetMapping("/date-range")
  public ResponseEntity<Page<AuditEvent>> getAuditEventsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<AuditEvent> auditEvents = auditService.getAuditEventsByDateRange(startDate, endDate, pageable);
    return ResponseEntity.ok(auditEvents);
  }

  /**
   * Get recent audit events for a specific user.
   *
   * @param username the username
   * @param limit    the maximum number of events to return (default: 10, max: 50)
   * @return list of recent audit events
   */
  @GetMapping("/user/{username}/recent")
  public ResponseEntity<List<AuditEvent>> getRecentAuditEventsForUser(
      @PathVariable String username,
      @RequestParam(defaultValue = "10") int limit) {
    // Limit the maximum number of events to prevent abuse
    int maxLimit = Math.min(limit, 50);
    List<AuditEvent> auditEvents = auditService.getRecentAuditEventsForUser(username, maxLimit);
    return ResponseEntity.ok(auditEvents);
  }

  /**
   * Get failed login attempts for a user.
   *
   * @param username the username
   * @return list of recent failed login attempts
   */
  @GetMapping("/user/{username}/failed-logins")
  public ResponseEntity<List<AuditEvent>> getFailedLoginAttempts(@PathVariable String username) {
    List<AuditEvent> failedAttempts = auditService.getRecentFailedLoginAttempts(username);
    return ResponseEntity.ok(failedAttempts);
  }

  /**
   * Search audit events with multiple filters.
   *
   * @param username  optional username filter
   * @param eventType optional event type filter
   * @param action    optional action filter
   * @param status    optional status filter
   * @param startDate optional start date filter
   * @param endDate   optional end date filter
   * @param pageable  pagination parameters
   * @return paginated list of filtered audit events
   */
  @GetMapping("/search")
  public ResponseEntity<Page<AuditEvent>> searchAuditEvents(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

    // For this example, we'll implement basic filtering
    // In a real application, you might want to use Spring Data Specifications or
    // QueryDSL
    Page<AuditEvent> auditEvents;

    if (username != null && startDate != null && endDate != null) {
      auditEvents = auditService.getAuditEventsByUser(username, pageable);
    } else if (eventType != null) {
      auditEvents = auditService.getAuditEventsByType(eventType, pageable);
    } else if (startDate != null && endDate != null) {
      auditEvents = auditService.getAuditEventsByDateRange(startDate, endDate, pageable);
    } else {
      auditEvents = auditService.getAuditEvents(pageable);
    }

    return ResponseEntity.ok(auditEvents);
  }
}