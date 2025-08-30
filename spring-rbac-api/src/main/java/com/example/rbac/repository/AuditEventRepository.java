package com.example.rbac.repository;

import com.example.rbac.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditEvent entity operations.
 * Provides methods for querying audit logs with various filters.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

  /**
   * Find audit events by username.
   *
   * @param username the username to search for
   * @param pageable pagination information
   * @return page of audit events for the user
   */
  Page<AuditEvent> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

  /**
   * Find audit events by event type.
   *
   * @param eventType the event type to search for
   * @param pageable  pagination information
   * @return page of audit events of the specified type
   */
  Page<AuditEvent> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

  /**
   * Find audit events within a date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @param pageable  pagination information
   * @return page of audit events within the date range
   */
  Page<AuditEvent> findByTimestampBetweenOrderByTimestampDesc(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Find audit events by username and date range.
   *
   * @param username  the username
   * @param startDate the start date
   * @param endDate   the end date
   * @param pageable  pagination information
   * @return page of audit events for the user within the date range
   */
  Page<AuditEvent> findByUsernameAndTimestampBetweenOrderByTimestampDesc(
      String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Find audit events by action.
   *
   * @param action   the action to search for
   * @param pageable pagination information
   * @return page of audit events with the specified action
   */
  Page<AuditEvent> findByActionOrderByTimestampDesc(String action, Pageable pageable);

  /**
   * Find audit events by status.
   *
   * @param status   the status to search for
   * @param pageable pagination information
   * @return page of audit events with the specified status
   */
  Page<AuditEvent> findByStatusOrderByTimestampDesc(String status, Pageable pageable);

  /**
   * Find recent audit events for a user.
   *
   * @param username the username
   * @param limit    the maximum number of events to return
   * @return list of recent audit events for the user
   */
  @Query("SELECT a FROM AuditEvent a WHERE a.username = :username ORDER BY a.timestamp DESC")
  List<AuditEvent> findRecentByUsername(@Param("username") String username, Pageable pageable);

  /**
   * Count audit events by event type within a date range.
   *
   * @param eventType the event type
   * @param startDate the start date
   * @param endDate   the end date
   * @return count of audit events
   */
  @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.eventType = :eventType AND a.timestamp BETWEEN :startDate AND :endDate")
  long countByEventTypeAndTimestampBetween(
      @Param("eventType") String eventType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Find failed login attempts for a user within a time period.
   *
   * @param username  the username
   * @param startDate the start date
   * @return list of failed login audit events
   */
  @Query("SELECT a FROM AuditEvent a WHERE a.username = :username AND a.action = 'LOGIN' AND a.status = 'FAILURE' AND a.timestamp >= :startDate ORDER BY a.timestamp DESC")
  List<AuditEvent> findFailedLoginAttempts(
      @Param("username") String username,
      @Param("startDate") LocalDateTime startDate);
}