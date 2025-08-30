package com.example.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for the RBAC API.
 * Demonstrates Role-Based Access Control with JWT Authentication and audit
 * logging.
 */
@SpringBootApplication
@EnableAsync
public class SpringRbacApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringRbacApiApplication.class, args);
  }
}