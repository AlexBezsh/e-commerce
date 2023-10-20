package com.alexbezsh.ecommerce.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import static com.alexbezsh.ecommerce.TestUtils.USER_EMAIL;
import static com.alexbezsh.ecommerce.TestUtils.USER_ID;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserEmail;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public class SecurityUtilsTest {

    private static final String USER_ID_NOT_PRESENT = "User ID not present";
    private static final String USER_EMAIL_NOT_PRESENT = "User email not present";

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserIdTest() {
        mockUser();

        assertEquals(USER_ID, getUserId());
    }

    @Test
    void getUserIdShouldThrowExceptionIfIdIsNull() {
        mockUser(null, USER_EMAIL);

        RuntimeException e = assertThrows(RuntimeException.class, SecurityUtils::getUserId);

        assertEquals(USER_ID_NOT_PRESENT, e.getMessage());
    }

    @Test
    void getUserIdShouldThrowExceptionIfNoSecurityContext() {
        RuntimeException e = assertThrows(RuntimeException.class, SecurityUtils::getUserId);

        assertEquals(USER_ID_NOT_PRESENT, e.getMessage());
    }

    @Test
    void getUserEmailTest() {
        mockUser();

        assertEquals(USER_EMAIL, getUserEmail());
    }

    @Test
    void getUserEmailShouldThrowExceptionIfEmailIsNull() {
        mockUser(USER_ID, null);

        RuntimeException e = assertThrows(RuntimeException.class, SecurityUtils::getUserEmail);

        assertEquals(USER_EMAIL_NOT_PRESENT, e.getMessage());
    }

    @Test
    void getUserEmailShouldThrowExceptionIfNoSecurityContext() {
        RuntimeException e = assertThrows(RuntimeException.class, SecurityUtils::getUserEmail);

        assertEquals(USER_EMAIL_NOT_PRESENT, e.getMessage());
    }

    public static void mockUser() {
        mockUser(USER_ID, USER_EMAIL);
    }

    public static void mockUser(String id, String email) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        doReturn(authentication).when(securityContext).getAuthentication();
        doReturn(jwt).when(authentication).getPrincipal();
        lenient().doReturn(email).when(jwt).getClaimAsString("email");
        lenient().doReturn(id).when(jwt).getSubject();

        SecurityContextHolder.setContext(securityContext);
    }

}
