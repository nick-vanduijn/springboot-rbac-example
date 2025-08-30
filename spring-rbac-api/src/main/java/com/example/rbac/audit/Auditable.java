package com.example.rbac.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that should be audited.
 * When applied to a method, it will automatically log audit events.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

  /**
   * The type of event for audit logging.
   */
  String eventType() default "SYSTEM";

  /**
   * The action being performed.
   */
  String action() default "";

  /**
   * Whether to include method parameters in audit details.
   */
  boolean includeParameters() default false;

  /**
   * Whether to include return value in audit details.
   */
  boolean includeReturnValue() default false;

  /**
   * Resource type for resource-related operations.
   */
  String resourceType() default "";
}