package com.example.rbac.service;

import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserService implementing UserDetailsService for Spring Security integration.
 * Provides real RBAC functionality with role-based access control using
 * database repositories.
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  /**
   * Constructor with dependencies.
   *
   * @param passwordEncoder the password encoder for hashing passwords
   * @param userRepository  the user repository for database operations
   * @param roleRepository  the role repository for database operations
   */
  public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;

    // Initialize default roles if they don't exist
    initializeDefaultRoles();
  }

  /**
   * Initializes default roles in the database if they don't exist.
   */
  private void initializeDefaultRoles() {
    if (!roleRepository.existsByName("USER")) {
      Role userRole = new Role();
      userRole.setName("USER");
      userRole.setDescription("Default user role");
      roleRepository.save(userRole);
    }

    if (!roleRepository.existsByName("ADMIN")) {
      Role adminRole = new Role();
      adminRole.setName("ADMIN");
      adminRole.setDescription("Administrator role");
      roleRepository.save(adminRole);
    }
  }

  /**
   * Loads user by username for Spring Security authentication.
   *
   * @param username the username to search for
   * @return UserDetails implementation with user information and authorities
   * @throws UsernameNotFoundException if user is not found
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> userOptional = userRepository.findByUsername(username);

    if (userOptional.isEmpty()) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    User user = userOptional.get();

    if (user == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
        .collect(Collectors.toList());

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(authorities)
        .accountExpired(!user.isAccountNonExpired())
        .accountLocked(!user.isAccountNonLocked())
        .credentialsExpired(!user.isCredentialsNonExpired())
        .disabled(!user.isEnabled())
        .build();
  }

  /**
   * Creates a new user with encrypted password and default USER role.
   *
   * @param user the user to create
   * @return the created user with encrypted password
   */
  public User createUser(User user) {
    // Encrypt the password before storing
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    // Set account defaults
    user.setEnabled(true);
    user.setAccountNonExpired(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());

    // Assign default USER role if no roles are assigned
    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      Optional<Role> userRole = roleRepository.findByName("USER");
      if (userRole.isPresent()) {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole.get());
        user.setRoles(roles);
      }
    }

    // Save user to database
    return userRepository.save(user);
  }

  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return the user if found, null otherwise
   */
  public User findByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
  }

  /**
   * Checks if a user has a specific role.
   *
   * @param user     the user to check
   * @param roleName the name of the role
   * @return true if user has the role, false otherwise
   */
  public boolean hasRole(User user, String roleName) {
    return user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(roleName));
  }

  /**
   * Checks if a user has any of the specified roles.
   *
   * @param user      the user to check
   * @param roleNames the names of the roles to check
   * @return true if user has any of the roles, false otherwise
   */
  public boolean hasAnyRole(User user, String... roleNames) {
    for (String roleName : roleNames) {
      if (hasRole(user, roleName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a user has all of the specified roles.
   *
   * @param user      the user to check
   * @param roleNames the names of the roles to check
   * @return true if user has all of the roles, false otherwise
   */
  public boolean hasAllRoles(User user, String... roleNames) {
    for (String roleName : roleNames) {
      if (!hasRole(user, roleName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Updates a user's information.
   *
   * @param user the user to update
   * @return the updated user
   */
  public User updateUser(User user) {
    Optional<User> existingUserOpt = userRepository.findByUsername(user.getUsername());
    if (existingUserOpt.isPresent()) {
      User existingUser = existingUserOpt.get();

      // Update fields but preserve encrypted password if not changed
      if (!user.getPassword().equals(existingUser.getPassword())) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
      }

      user.setUpdatedAt(LocalDateTime.now());
      return userRepository.save(user);
    }
    return user;
  }

  /**
   * Deletes a user by username.
   *
   * @param username the username of the user to delete
   * @return true if user was deleted, false if not found
   */
  public boolean deleteUser(String username) {
    Optional<User> user = userRepository.findByUsername(username);
    if (user.isPresent()) {
      userRepository.delete(user.get());
      return true;
    }
    return false;
  }

  /**
   * Gets all users (for administrative purposes).
   *
   * @return a list of all users
   */
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Checks if a username already exists.
   *
   * @param username the username to check
   * @return true if username exists, false otherwise
   */
  public boolean usernameExists(String username) {
    return userRepository.existsByUsername(username);
  }

  /**
   * Checks if an email already exists.
   *
   * @param email the email to check
   * @return true if email exists, false otherwise
   */
  public boolean emailExists(String email) {
    return userRepository.existsByEmail(email);
  }
}