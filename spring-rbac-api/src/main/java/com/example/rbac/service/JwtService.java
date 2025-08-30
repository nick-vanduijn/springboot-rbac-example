package com.example.rbac.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for handling JWT token creation, validation, and extraction.
 * Uses real JWT implementation with actual cryptographic operations.
 */
@Service
public class JwtService {

  /**
   * Secret key for JWT signing. In production, this should be externalized.
   */
  private static final String SECRET_KEY = "4D6351665468576D5A7134743777217A25432A46294A404E635266556A586E32";

  /**
   * Token expiration time in milliseconds (24 hours).
   */
  private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

  /**
   * Extracts the username from the JWT token.
   *
   * @param token the JWT token
   * @return the username contained in the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date from the JWT token.
   *
   * @param token the JWT token
   * @return the expiration date
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim from the JWT token.
   *
   * @param token          the JWT token
   * @param claimsResolver function to extract the desired claim
   * @param <T>            the type of the claim
   * @return the extracted claim
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Generates a JWT token for the given user details.
   *
   * @param userDetails the user details
   * @return the generated JWT token
   */
  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /**
   * Generates a JWT token with extra claims for the given user details.
   *
   * @param extraClaims additional claims to include in the token
   * @param userDetails the user details
   * @return the generated JWT token
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, JWT_EXPIRATION);
  }

  /**
   * Validates the JWT token against the user details.
   *
   * @param token       the JWT token
   * @param userDetails the user details to validate against
   * @return true if the token is valid, false otherwise
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  /**
   * Checks if the JWT token is expired.
   *
   * @param token the JWT token
   * @return true if the token is expired, false otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Builds a JWT token with the specified claims, user details, and expiration.
   *
   * @param extraClaims additional claims
   * @param userDetails user details
   * @param expiration  expiration time in milliseconds
   * @return the built JWT token
   */
  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts
        .builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey())
        .compact();
  }

  /**
   * Extracts all claims from the JWT token.
   *
   * @param token the JWT token
   * @return all claims contained in the token
   */
  private Claims extractAllClaims(String token) {
    return Jwts
        .parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Gets the signing key for JWT operations.
   *
   * @return the signing key
   */
  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}