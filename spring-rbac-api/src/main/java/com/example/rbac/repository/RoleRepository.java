package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity operations.
 * Provides methods for database access related to roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Find a role by name.
   * 
   * @param name the role name to search for
   * @return Optional containing the role if found, empty otherwise
   */
  Optional<Role> findByName(String name);

  /**
   * Check if a role exists with the given name.
   * 
   * @param name the role name to check
   * @return true if role exists, false otherwise
   */
  boolean existsByName(String name);
}