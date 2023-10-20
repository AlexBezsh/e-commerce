package com.alexbezsh.ecommerce.utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@UtilityClass
public class SecurityUtils {

    public static final String USER_ROLE = "USER";

    public static String getUserId() {
        return jwt()
            .map(Jwt::getSubject)
            .orElseThrow(() -> new RuntimeException("User ID not present"));
    }

    public static String getUserEmail() {
        return jwt()
            .map(jwt -> jwt.getClaimAsString("email"))
            .orElseThrow(() -> new RuntimeException("User email not present"));
    }

    private static Optional<Jwt> jwt() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .map(Jwt.class::cast);
    }

}
