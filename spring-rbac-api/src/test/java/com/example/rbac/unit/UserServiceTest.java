package com.example.rbac.unit;

import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import com.example.rbac.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User Service with real RBAC functionality.
 * Tests actual role-based access control with database repositories.
 */
@DataJpaTest
@SpringJUnitConfig
class UserServiceTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService(UserRepository userRepository, RoleRepository roleRepository,
        PasswordEncoder passwordEncoder) {
      return new UserService(passwordEncoder, userRepository, roleRepository);
    }
  }

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    // Clear any existing data
    userRepository.deleteAll();
    roleRepository.deleteAll();

    setupTestUsers();
  }

  private void setupTestUsers() {
    // Create or find roles (check if they exist first)
    Role adminRole = roleRepository.findByName("ADMIN")
        .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Administrator role")));
    Role userRole = roleRepository.findByName("USER")
        .orElseGet(() -> roleRepository.save(new Role("USER", "Regular user role")));
    Role moderatorRole = roleRepository.findByName("MODERATOR")
        .orElseGet(() -> roleRepository.save(new Role("MODERATOR", "Moderator role")));

    // Create users with different roles (only if they don't exist)
    if (!userRepository.existsByUsername("admin")) {
      User adminUser = new User("admin", "admin@example.com", "admin123");
      adminUser.setRoles(Set.of(adminRole, userRole)); // Admin also has user permissions
      userService.createUser(adminUser);
    }

    if (!userRepository.existsByUsername("user")) {
      User regularUser = new User("user", "user@example.com", "user123");
      regularUser.setRoles(Set.of(userRole));
      userService.createUser(regularUser);
    }

    if (!userRepository.existsByUsername("moderator")) {
      User moderatorUser = new User("moderator", "mod@example.com", "mod123");
      moderatorUser.setRoles(Set.of(moderatorRole, userRole));
      userService.createUser(moderatorUser);
    }
  }

  @Test
  void shouldLoadUserByUsernameSuccessfully() {
    // Given a valid username
    String username = "admin";

    // When loading user details
    UserDetails userDetails = userService.loadUserByUsername(username);

    // Then user should be loaded with correct details
    assertNotNull(userDetails);
    assertEquals(username, userDetails.getUsername());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
  }

  @Test
  void shouldLoadUserWithCorrectAuthorities() {
    // Given an admin user
    String username = "admin";

    // When loading user details
    UserDetails userDetails = userService.loadUserByUsername(username);

    // Then user should have correct authorities
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertEquals(2, userDetails.getAuthorities().size());
  }

  @Test
  void shouldLoadRegularUserWithLimitedAuthorities() {
    // Given a regular user
    String username = "user";

    // When loading user details
    UserDetails userDetails = userService.loadUserByUsername(username);

    // Then user should have only user role
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertEquals(1, userDetails.getAuthorities().size());
  }

  @Test
  void shouldLoadModeratorWithCorrectAuthorities() {
    // Given a moderator user
    String username = "moderator";

    // When loading user details
    UserDetails userDetails = userService.loadUserByUsername(username);

    // Then user should have moderator and user roles
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertEquals(2, userDetails.getAuthorities().size());
  }

  @Test
  void shouldThrowExceptionForNonExistentUser() {
    // Given a non-existent username
    String username = "nonexistent";

    // When loading user details
    // Then should throw UsernameNotFoundException
    assertThrows(UsernameNotFoundException.class, () -> {
      userService.loadUserByUsername(username);
    });
  }

  @Test
  void shouldCreateUserWithEncryptedPassword() {
    // Given a new user
    User newUser = new User("newuser", "new@example.com", "plainPassword");
    Role userRole = roleRepository.findByName("USER").orElse(new Role("USER", "Regular user role"));
    newUser.setRoles(Set.of(userRole));

    // When creating the user
    User createdUser = userService.createUser(newUser);

    // Then password should be encrypted
    assertNotNull(createdUser);
    assertNotEquals("plainPassword", createdUser.getPassword());
    assertTrue(passwordEncoder.matches("plainPassword", createdUser.getPassword()));
  }

  @Test
  void shouldCheckUserHasRole() {
    // Given an admin user
    User adminUser = userService.findByUsername("admin");

    // When checking roles
    boolean hasAdminRole = userService.hasRole(adminUser, "ADMIN");
    boolean hasUserRole = userService.hasRole(adminUser, "USER");
    boolean hasModeratorRole = userService.hasRole(adminUser, "MODERATOR");

    // Then should correctly identify roles
    assertTrue(hasAdminRole);
    assertTrue(hasUserRole);
    assertFalse(hasModeratorRole);
  }

  @Test
  void shouldCheckUserHasAnyRole() {
    // Given a moderator user
    User moderatorUser = userService.findByUsername("moderator");

    // When checking if user has any of specified roles
    boolean hasAnyAdminOrMod = userService.hasAnyRole(moderatorUser, "ADMIN", "MODERATOR");
    boolean hasAnyAdminOrGuest = userService.hasAnyRole(moderatorUser, "ADMIN", "GUEST");

    // Then should correctly identify role presence
    assertTrue(hasAnyAdminOrMod);
    assertFalse(hasAnyAdminOrGuest);
  }
}