package com.uc.ms_security.authorization;

import com.uc.ms_security.service.PermissionService;
import com.uc.ms_security.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

@Component
public class DynamicAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private static final Pattern USER_PROFILE_PATH =
            Pattern.compile("^/api/users/(\\d+)/profile$");
    private static final Pattern USER_DETAIL_PATH =
            Pattern.compile("^/api/users/(\\d+)/detail$");
    private static final Pattern USER_ROLES_PATH =
            Pattern.compile("^/api/users/(\\d+)/detail-with-roles$");
    private static final Pattern USER_ROLE_ASSIGNMENTS_PATH =
            Pattern.compile("^/api/user-roles/users/(\\d+)$");
    private final PermissionService permissionService;
    private final SessionService sessionService;

    @Autowired
    public DynamicAuthorizationManager(
            PermissionService permissionService,
            SessionService sessionService) {
        this.permissionService = permissionService;
        this.sessionService = sessionService;
    }

    public DynamicAuthorizationManager(PermissionService permissionService) {
        this(permissionService, null);
    }

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        if (sessionService != null
                && authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwt
                && !sessionService.isActive(jwt.getToken().getTokenValue())) {
            return new AuthorizationDecision(false);
        }

        final long userId;
        try {
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            return new AuthorizationDecision(false);
        }

        String requestUri = context.getRequest().getRequestURI();
        String method = context.getRequest().getMethod();
        if (isSelfServiceRequest(userId, method, requestUri)) {
            return new AuthorizationDecision(true);
        }

        boolean permitted = permissionService.hasPermission(
                userId,
                method,
                requestUri);
        return new AuthorizationDecision(permitted);
    }

    private boolean isSelfServiceRequest(
            long authenticatedUserId,
            String method,
            String requestUri) {
        if ("GET".equals(method)
                && matchesOwnUserPath(
                        authenticatedUserId, USER_DETAIL_PATH, requestUri)) {
            return true;
        }

        if ("GET".equals(method)
                && (matchesOwnUserPath(authenticatedUserId, USER_ROLES_PATH, requestUri)
                        || matchesOwnUserPath(
                                authenticatedUserId,
                                USER_ROLE_ASSIGNMENTS_PATH,
                                requestUri))) {
            return true;
        }

        if (isOwnTwoFactorPreference(method, requestUri)) {
            return true;
        }

        return matchesOwnUserPath(authenticatedUserId, USER_PROFILE_PATH, requestUri)
                && isSelfServiceProfileMethod(method);
    }

    private boolean matchesOwnUserPath(
            long authenticatedUserId,
            Pattern pathPattern,
            String requestUri) {
        Matcher path = pathPattern.matcher(requestUri);
        return path.matches()
                && Long.toString(authenticatedUserId).equals(path.group(1));
    }

    private boolean isSelfServiceProfileMethod(String method) {
        return "GET".equals(method)
                || "POST".equals(method)
                || "PUT".equals(method);
    }

    private boolean isOwnTwoFactorPreference(String method, String requestUri) {
        return "/api/auth/2fa".equals(requestUri)
                && ("GET".equals(method) || "PUT".equals(method));
    }
}
