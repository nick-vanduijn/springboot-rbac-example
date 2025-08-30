package com.example.rbac.service;

import com.example.rbac.entity.AuditEvent;
import com.example.rbac.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit events and providing audit logging functionality.
 * Handles creation, storage, and retrieval of audit logs for security and
 * compliance.
 */
@Service
@Transactional
public class AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  private final AuditEventRepository auditEventRepository;

  @Autowired
  public AuditService(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  /**
   * Log an audit event asynchronously.
   *
   * @param eventType the type of event
   * @param action    the action performed
   * @param details   additional details about the event
   */
  @Async
  public void logEvent(String eventType, String action, String details) {
    try {
      String username = getCurrentUsername();
      HttpServletRequest request = getCurrentRequest();

      AuditEvent auditEvent = new AuditEvent(eventType, action, username);
      auditEvent.setDetails(details);

      if (request != null) {
        auditEvent.setIpAddress(getClientIpAddress(request));
        auditEvent.setUserAgent(request.getHeader("User-Agent"));
      }

      auditEventRepository.save(auditEvent);
      logger.debug("Audit event logged: {} - {} by {}", eventType, action, username);
    } catch (Exception e) {
      logger.error("Failed to log audit event: {} - {}", eventType, action, e);
    }
  }

  /**
   * Log an audit event with specific status.
   *
   * @param eventType the type of event
   * @param action    the action performed
   * @param details   additional details
   * @param status    the status of the event (SUCCESS, FAILURE, ERROR)
   */
  @Async
  public void logEvent(String eventType, String action, String details, String status) {
    try {
      String username = getCurrentUsername();
      HttpServletRequest request = getCurrentRequest();

      AuditEvent auditEvent = new AuditEvent(eventType, action, username);
      auditEvent.setDetails(details);
      auditEvent.setStatus(status);

      if (request != null) {
        auditEvent.setIpAddress(getClientIpAddress(request));
        auditEvent.setUserAgent(request.getHeader("User-Agent"));
      }

      auditEventRepository.save(auditEvent);
      logger.debug("Audit event logged: {} - {} by {} [{}]", eventType, action, username, status);
    } catch (Exception e) {
      logger.error("Failed to log audit event: {} - {}", eventType, action, e);
    }
  }

  /**
   * Log an audit event for a specific user.
   *
   * @param eventType the type of event
   * @param action    the action performed
   * @param username  the username
   * @param details   additional details
   */
  @Async
  public void logEventForUser(String eventType, String action, String username, String details) {
    try {
      HttpServletRequest request = getCurrentRequest();

      AuditEvent auditEvent = new AuditEvent(eventType, action, username);
      auditEvent.setDetails(details);

      if (request != null) {
        auditEvent.setIpAddress(getClientIpAddress(request));
        auditEvent.setUserAgent(request.getHeader("User-Agent"));
      }

      auditEventRepository.save(auditEvent);
      logger.debug("Audit event logged for user {}: {} - {}", username, eventType, action);
    } catch (Exception e) {
      logger.error("Failed to log audit event for user {}: {} - {}", username, eventType, action, e);
    }
  }

  /**
   * Log a resource-related audit event.
   *
   * @param eventType    the type of event
   * @param action       the action performed
   * @param resourceType the type of resource
   * @param resourceId   the ID of the resource
   * @param details      additional details
   */
  @Async
  public void logResourceEvent(String eventType, String action, String resourceType, Long resourceId, String details) {
    try {
      String username = getCurrentUsername();
      HttpServletRequest request = getCurrentRequest();

      AuditEvent auditEvent = new AuditEvent(eventType, action, username);
      auditEvent.setDetails(details);
      auditEvent.setResourceType(resourceType);
      auditEvent.setResourceId(resourceId);

      if (request != null) {
        auditEvent.setIpAddress(getClientIpAddress(request));
        auditEvent.setUserAgent(request.getHeader("User-Agent"));
      }

      auditEventRepository.save(auditEvent);
      logger.debug("Resource audit event logged: {} {} {} (ID: {}) by {}",
          eventType, action, resourceType, resourceId, username);
    } catch (Exception e) {
      logger.error("Failed to log resource audit event: {} - {}", eventType, action, e);
    }
  }

  /**
   * Get audit events with pagination.
   *
   * @param pageable pagination information
   * @return page of audit events
   */
  @Transactional(readOnly = true)
  public Page<AuditEvent> getAuditEvents(Pageable pageable) {
    return auditEventRepository.findAll(pageable);
  }

  /**
   * Get audit events for a specific user.
   *
   * @param username the username
   * @param pageable pagination information
   * @return page of audit events for the user
   */
  @Transactional(readOnly = true)
  public Page<AuditEvent> getAuditEventsByUser(String username, Pageable pageable) {
    return auditEventRepository.findByUsernameOrderByTimestampDesc(username, pageable);
  }

  /**
   * Get audit events by type.
   *
   * @param eventType the event type
   * @param pageable  pagination information
   * @return page of audit events of the specified type
   */
  @Transactional(readOnly = true)
  public Page<AuditEvent> getAuditEventsByType(String eventType, Pageable pageable) {
    return auditEventRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
  }

  /**
   * Get audit events within a date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @param pageable  pagination information
   * @return page of audit events within the date range
   */
  @Transactional(readOnly = true)
  public Page<AuditEvent> getAuditEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return auditEventRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
  }

  /**
   * Get recent audit events for a user.
   *
   * @param username the username
   * @param limit    the maximum number of events to return
   * @return list of recent audit events
   */
  @Transactional(readOnly = true)
  public List<AuditEvent> getRecentAuditEventsForUser(String username, int limit) {
    return auditEventRepository.findRecentByUsername(username,
        org.springframework.data.domain.PageRequest.of(0, limit));
  }

  /**
   * Get failed login attempts for a user within the last hour.
   *
   * @param username the username
   * @return list of failed login attempts
   */
  @Transactional(readOnly = true)
  public List<AuditEvent> getRecentFailedLoginAttempts(String username) {
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    return auditEventRepository.findFailedLoginAttempts(username, oneHourAgo);
  }

  /**
   * Get the current authenticated username.
   *
   * @return the current username or "anonymous" if not authenticated
   */
  private String getCurrentUsername() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated() &&
          !"anonymousUser".equals(authentication.getPrincipal())) {
        return authentication.getName();
      }
    } catch (Exception e) {
      logger.debug("Could not get current username: {}", e.getMessage());
    }
    return "anonymous";
  }

  /**
   * Get the current HTTP request.
   *
   * @return the current HttpServletRequest or null if not available
   */
  private HttpServletRequest getCurrentRequest() {
    try {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attributes != null ? attributes.getRequest() : null;
    } catch (Exception e) {
      logger.debug("Could not get current request: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract the client IP address from the request.
   *
   * @param request the HTTP request
   * @return the client IP address
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String[] headers = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };

    for (String header : headers) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // Handle multiple IPs in X-Forwarded-For
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        return ip;
      }
    }

    return request.getRemoteAddr();
  }
}