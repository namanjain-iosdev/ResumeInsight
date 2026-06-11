package com.cvanalyzer.security;

import com.cvanalyzer.exception.UnauthorizedException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Verifies a Google ID token by delegating to Google's public tokeninfo
 * endpoint. This keeps the integration dependency-free (uses the existing
 * RestTemplate) while still validating signature/expiry/issuer server-side.
 *
 * <p>If {@code app.google.client-id} is configured, the token audience is
 * checked against it as an extra guard.
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.client-id:}")
    private String expectedClientId;

    @SuppressWarnings("unchecked")
    public GoogleUser verify(String idToken) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(TOKENINFO_URL)
                    .queryParam("id_token", idToken)
                    .toUriString();
            Map<String, Object> claims = restTemplate.getForObject(url, Map.class);
            if (claims == null || claims.get("sub") == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            String issuer = str(claims.get("iss"));
            if (issuer == null || !(issuer.contains("accounts.google.com"))) {
                throw new UnauthorizedException("Untrusted token issuer");
            }

            if (expectedClientId != null && !expectedClientId.isBlank()) {
                String aud = str(claims.get("aud"));
                if (!expectedClientId.equals(aud)) {
                    throw new UnauthorizedException("Google token audience mismatch");
                }
            }

            GoogleUser user = new GoogleUser();
            user.setSub(str(claims.get("sub")));
            user.setEmail(str(claims.get("email")));
            user.setName(str(claims.get("name")));
            user.setPicture(str(claims.get("picture")));
            user.setEmailVerified("true".equalsIgnoreCase(str(claims.get("email_verified"))));

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new UnauthorizedException("Google token did not contain an email");
            }
            return user;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            throw new UnauthorizedException("Could not verify Google login. Please try again.");
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    @Data
    public static class GoogleUser {
        private String sub;
        private String email;
        private String name;
        private String picture;
        private boolean emailVerified;
    }
}
