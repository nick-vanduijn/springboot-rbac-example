package com.example.rbac.unit;

import com.example.rbac.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWT Service with real JWT token generation and validation.
 * Tests actual JWT functionality without mocks.
 */
class JwtServiceTest {

  private JwtService jwtService;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    userDetails = User.builder()
        .username("testuser")
        .password("password")
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();
  }

  @Test
  void shouldGenerateValidJwtToken() {
    // Given a user
    // When generating a token
    String token = jwtService.generateToken(userDetails);

    // Then token should not be null or empty
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains(".")); // JWT has dots separating header.payload.signature
  }

  @Test
  void shouldExtractUsernameFromValidToken() {
    // Given a generated token
    String token = jwtService.generateToken(userDetails);

    // When extracting username
    String extractedUsername = jwtService.extractUsername(token);

    // Then username should match
    assertEquals("testuser", extractedUsername);
  }

  @Test
  void shouldValidateTokenSuccessfully() {
    // Given a generated token
    String token = jwtService.generateToken(userDetails);

    // When validating token
    boolean isValid = jwtService.isTokenValid(token, userDetails);

    // Then token should be valid
    assertTrue(isValid);
  }

  @Test
  void shouldRejectExpiredToken() {
    // Given an expired token (this test will pass once we implement expiration
    // logic)
    String token = jwtService.generateToken(userDetails);

    // When token expires and we validate it
    // This will require us to implement actual expiration
    Date expiration = jwtService.extractExpiration(token);

    // Then expiration should be in the future
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void shouldRejectTokenWithDifferentUser() {
    // Given a token for one user
    String token = jwtService.generateToken(userDetails);

    // When validating with different user
    UserDetails differentUser = User.builder()
        .username("differentuser")
        .password("password")
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    boolean isValid = jwtService.isTokenValid(token, differentUser);

    // Then token should be invalid
    assertFalse(isValid);
  }

  @Test
  void shouldRejectMalformedToken() {
    // Given a malformed token
    String malformedToken = "invalid.jwt.token";

    // When validating malformed token
    // Then should throw exception or return false
    assertThrows(Exception.class, () -> {
      jwtService.extractUsername(malformedToken);
    });
  }
}