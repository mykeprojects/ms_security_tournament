package com.uc.ms_security.authorization;

import com.uc.ms_security.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicAuthorizationManagerTest {

    private PermissionService permissionService;
    private DynamicAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        authorizationManager =
                new DynamicAuthorizationManager(permissionService);
    }

    @Test
    void allowsAuthenticatedUserToManageOwnProfile() {
        Authentication authentication = authenticatedAs("42");
        RequestAuthorizationContext context = requestContext(
                "PUT", "/api/users/42/profile");

        assertTrue(authorizationManager.authorize(
                () -> authentication, context).isGranted());
    }

    @Test
    void deniesUserAccessToAnotherUsersProfileWithoutPermission() {
        Authentication authentication = authenticatedAs("42");
        RequestAuthorizationContext context = requestContext(
                "PUT", "/api/users/7/profile");

        assertFalse(authorizationManager.authorize(
                () -> authentication, context).isGranted());
    }

    @Test
    void doesNotAllowSelfServiceProfileDeletion() {
        Authentication authentication = authenticatedAs("42");
        RequestAuthorizationContext context = requestContext(
                "DELETE", "/api/users/42/profile");

        assertFalse(authorizationManager.authorize(
                () -> authentication, context).isGranted());
    }

    private Authentication authenticatedAs(String userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);
        return authentication;
    }

    private RequestAuthorizationContext requestContext(
            String method,
            String requestUri) {
        MockHttpServletRequest request =
                new MockHttpServletRequest(method, requestUri);
        return new RequestAuthorizationContext(request);
    }
}
