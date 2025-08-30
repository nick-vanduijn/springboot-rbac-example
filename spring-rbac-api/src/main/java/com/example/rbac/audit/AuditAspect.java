package com.example.rbac.audit;

import com.example.rbac.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for automatic audit logging using AOP.
 * Intercepts methods annotated with @Auditable and logs audit events.
 */
@Aspect
@Component
public class AuditAspect {

  private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

  private final AuditService auditService;

  public AuditAspect(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Advice that logs audit events after successful method execution.
   *
   * @param joinPoint   the join point
   * @param auditable   the auditable annotation
   * @param returnValue the method return value
   */
  @AfterReturning(pointcut = "@annotation(auditable)", returning = "returnValue")
  public void logAuditEventAfterReturning(JoinPoint joinPoint, Auditable auditable, Object returnValue) {
    try {
      String eventType = getEventType(auditable, joinPoint);
      String action = getAction(auditable, joinPoint);
      String details = buildAuditDetails(joinPoint, auditable, returnValue, null);

      auditService.logEvent(eventType, action, details, "SUCCESS");

      logger.debug("Audit event logged for successful execution: {}.{}",
          joinPoint.getSignature().getDeclaringTypeName(),
          joinPoint.getSignature().getName());
    } catch (Exception e) {
      logger.error("Failed to log audit event for successful execution", e);
    }
  }

  /**
   * Advice that logs audit events after method throws an exception.
   *
   * @param joinPoint the join point
   * @param auditable the auditable annotation
   * @param exception the thrown exception
   */
  @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
  public void logAuditEventAfterThrowing(JoinPoint joinPoint, Auditable auditable, Exception exception) {
    try {
      String eventType = getEventType(auditable, joinPoint);
      String action = getAction(auditable, joinPoint);
      String details = buildAuditDetails(joinPoint, auditable, null, exception);

      auditService.logEvent(eventType, action, details, "FAILURE");

      logger.debug("Audit event logged for failed execution: {}.{}",
          joinPoint.getSignature().getDeclaringTypeName(),
          joinPoint.getSignature().getName());
    } catch (Exception e) {
      logger.error("Failed to log audit event for failed execution", e);
    }
  }

  /**
   * Determine the event type from annotation or method context.
   */
  private String getEventType(Auditable auditable, JoinPoint joinPoint) {
    if (!auditable.eventType().isEmpty() && !"SYSTEM".equals(auditable.eventType())) {
      return auditable.eventType();
    }

    // Try to infer event type from class name
    String className = joinPoint.getSignature().getDeclaringTypeName();
    if (className.contains("Controller")) {
      return "API";
    } else if (className.contains("Service")) {
      return "SERVICE";
    } else if (className.contains("Repository")) {
      return "DATA";
    }

    return "SYSTEM";
  }

  /**
   * Determine the action from annotation or method name.
   */
  private String getAction(Auditable auditable, JoinPoint joinPoint) {
    if (!auditable.action().isEmpty()) {
      return auditable.action();
    }

    // Use method name as action
    return joinPoint.getSignature().getName().toUpperCase();
  }

  /**
   * Build audit details including parameters, return value, and exception
   * information.
   */
  private String buildAuditDetails(JoinPoint joinPoint, Auditable auditable, Object returnValue, Exception exception) {
    StringBuilder details = new StringBuilder();

    details.append("Method: ").append(joinPoint.getSignature().getName());

    if (auditable.includeParameters() && joinPoint.getArgs().length > 0) {
      details.append(", Parameters: ").append(Arrays.toString(joinPoint.getArgs()));
    }

    if (auditable.includeReturnValue() && returnValue != null) {
      details.append(", ReturnValue: ").append(returnValue.toString());
    }

    if (exception != null) {
      details.append(", Exception: ").append(exception.getClass().getSimpleName())
          .append(" - ").append(exception.getMessage());
    }

    // Truncate details if too long
    String result = details.toString();
    if (result.length() > 500) {
      result = result.substring(0, 497) + "...";
    }

    return result;
  }
}