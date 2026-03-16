package com.codearena.backend.utils.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JWTHelper {

    private static final Logger logger = LoggerFactory.getLogger(JWTHelper.class);

    // Store the key as an instance variable to ensure consistency
    private static SecretKey signingKey = null;

    // 24 hours
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    // Default secret key (64 characters = 48 bytes when decoded)
    // Generate your own with: openssl rand -base64 48
    private static final String DEFAULT_SECRET_KEY = "c2VjcmV0LWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tMTIzNDU2Nzg5MA==";

    @Value("${jwt.secret:#{null}}")
    private String jwtSecret;

    // Initialize the signing key once (synchronized to prevent race conditions)
    private synchronized SecretKey getSigningKey() {
        if (signingKey != null) {
            return signingKey;
        }

        try {
            logger.debug("🔄 Creating signing key from secret...");

            // Use environment variable or default
            String secretToUse = (jwtSecret != null && !jwtSecret.trim().isEmpty())
                    ? jwtSecret
                    : DEFAULT_SECRET_KEY;

            logger.debug("Using secret key (first 20 chars): {}...",
                    secretToUse.substring(0, Math.min(20, secretToUse.length())));

            // Decode base64
            byte[] keyBytes;
            try {
                keyBytes = Decoders.BASE64.decode(secretToUse);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to decode as base64, using string bytes directly");
                keyBytes = secretToUse.getBytes();
            }

            logger.debug("Key bytes length: {} bytes", keyBytes.length);

            // Ensure minimum length for HS512 (64 bytes)
            if (keyBytes.length < 64) {
                logger.warn("⚠️ Key length {} bytes is less than 64 bytes for HS512", keyBytes.length);

                // Create a 64-byte array
                byte[] adjustedKey = new byte[64];
                // Copy existing bytes
                int copyLength = Math.min(keyBytes.length, 64);
                System.arraycopy(keyBytes, 0, adjustedKey, 0, copyLength);

                // If we need more bytes, fill with deterministic pattern
                if (copyLength < 64) {
                    for (int i = copyLength; i < 64; i++) {
                        adjustedKey[i] = (byte) (i % 256);
                    }
                }

                keyBytes = adjustedKey;
                logger.debug("Adjusted key to {} bytes", keyBytes.length);
            }

            // Create the key
            signingKey = Keys.hmacShaKeyFor(keyBytes);

            logger.debug("✅ Signing key created successfully");
            logger.debug("Key algorithm: {}", signingKey.getAlgorithm());
            logger.debug("Key encoded length: {} bytes", signingKey.getEncoded().length);

            return signingKey;

        } catch (Exception e) {
            logger.error("❌ Failed to create signing key: {}", e.getMessage(), e);

            // Ultimate fallback: generate a deterministic key
            logger.warn("⚠️ Using deterministic fallback key");
            String fallbackString = "CodeArena-JWT-Secret-Key-Fallback-2024-For-HS512-Algorithm";
            byte[] fallbackBytes = new byte[64];
            byte[] stringBytes = fallbackString.getBytes();
            System.arraycopy(stringBytes, 0, fallbackBytes, 0, Math.min(stringBytes.length, 64));

            // Fill remaining bytes
            for (int i = stringBytes.length; i < 64; i++) {
                fallbackBytes[i] = (byte) i;
            }

            signingKey = Keys.hmacShaKeyFor(fallbackBytes);
            logger.warn("⚠️ Fallback key created - CHANGE THIS IN PRODUCTION!");

            return signingKey;
        }
    }

    // Retrieve username from JWT token
    public String getUsernameFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token is null or empty");
                return null;
            }

            logger.debug("Parsing token to get username...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            logger.debug("✅ Extracted username from token: {}", username);
            return username;

        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            return e.getClaims().getSubject(); // Return subject even if expired
        } catch (SignatureException e) {
            logger.error("❌ Invalid JWT signature: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            logger.error("❌ Malformed JWT: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("❌ Failed to parse token: {}", e.getMessage());
            return null;
        }
    }

    // Generate token for user
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null) {
            logger.error("UserDetails is null!");
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        String username = userDetails.getUsername();
        logger.info("🔄 Generating JWT token for user: {}", username);

        Map<String, Object> claims = new HashMap<>();

        try {
            // Get authorities
            List<String> authorityList = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String authoritiesString = String.join(",", authorityList);

            logger.debug("User authorities: {}", authoritiesString);

            // Store claims - IMPORTANT: Store roles as array for frontend compatibility
            claims.put("authorities", authoritiesString);
            claims.put("roles", authorityList); // Store as List<String> for frontend
            claims.put("role", authorityList.get(0)); // Single role for compatibility
            claims.put("email", username);
            claims.put("sub", username);
            claims.put("iss", "CodeArena");
            claims.put("iat", System.currentTimeMillis() / 1000); // Issued at in seconds
            claims.put("jti", UUID.randomUUID().toString()); // Unique ID

            logger.debug("Claims prepared for user {}: {}", username, claims);

            String token = doGenerateToken(claims, username);

            if (token == null || token.isEmpty()) {
                logger.error("❌ Generated token is null or empty!");
                throw new RuntimeException("Generated token is empty");
            }

            // Verify the token can be parsed back
            try {
                String parsedUsername = getUsernameFromToken(token);
                if (parsedUsername != null && parsedUsername.equals(username)) {
                    logger.info("✅ Token generated and verified successfully for {}", username);
                } else {
                    logger.warn("⚠️ Generated token verification failed - parsed: {}, expected: {}",
                            parsedUsername, username);
                }
            } catch (Exception e) {
                logger.error("❌ Generated token cannot be parsed: {}", e.getMessage());
            }

            logger.debug("Token length: {}", token.length());
            logger.debug("Token preview: {}...", token.substring(0, Math.min(50, token.length())));

            return token;

        } catch (Exception e) {
            logger.error("❌ Failed to generate token for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to generate authentication token: " + e.getMessage());
        }
    }

    // Create the actual token
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        logger.debug("Creating JWT token for subject: {}", subject);

        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + JWT_TOKEN_VALIDITY);

            logger.debug("Token issued at: {}, expires at: {}", now, expiry);

            SecretKey key = getSigningKey();
            logger.debug("Using key with algorithm: {}", key.getAlgorithm());

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuer("CodeArena")
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();

            logger.debug("✅ JWT built successfully");

            // Debug token structure
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                logger.debug("Token structure - Parts: 3, Total length: {}", token.length());
                logger.debug("Header length: {}, Payload length: {}, Signature length: {}",
                        parts[0].length(), parts[1].length(), parts[2].length());

                // Decode and log the payload for debugging
                try {
                    String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    logger.debug("Token payload: {}", payloadJson);
                } catch (Exception e) {
                    logger.debug("Could not decode payload: {}", e.getMessage());
                }
            } else {
                logger.error("❌ Token has {} parts instead of 3!", parts.length);
            }

            return token;

        } catch (Exception e) {
            logger.error("❌ JWT builder failed: {}", e.getMessage(), e);
            throw new RuntimeException("JWT generation failed: " + e.getMessage());
        }
    }

    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            logger.warn("Token or UserDetails is null");
            return false;
        }

        try {
            final String username = getUsernameFromToken(token);
            boolean isValid = (username != null && username.equals(userDetails.getUsername()));

            if (isValid) {
                logger.debug("✅ Token validation successful for user: {}", username);
            } else {
                logger.warn("❌ Token validation failed: username mismatch");
                logger.warn("Token username: {}, UserDetails username: {}", username, userDetails.getUsername());
            }

            return isValid;
        } catch (Exception e) {
            logger.error("❌ Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // Additional helper method to get all claims
    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Failed to get claims from token: {}", e.getMessage());
            return null;
        }
    }
}