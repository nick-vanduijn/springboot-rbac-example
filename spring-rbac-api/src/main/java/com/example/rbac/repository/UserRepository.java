package com.example.rbac.repository;

import com.example.rbac.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides methods for database access related to users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find a user by username.
   * 
   * @param username the username to search for
   * @return Optional containing the user if found, empty otherwise
   */
  Optional<User> findByUsername(String username);

  /**
   * Find a user by email address.
   * 
   * @param email the email to search for
   * @return Optional containing the user if found, empty otherwise
   */
  Optional<User> findByEmail(String email);

  /**
   * Check if a user exists with the given username.
   * 
   * @param username the username to check
   * @return true if user exists, false otherwise
   */
  boolean existsByUsername(String username);

  /**
   * Check if a user exists with the given email address.
   * 
   * @param email the email to check
   * @return true if user exists, false otherwise
   */
  boolean existsByEmail(String email);
}