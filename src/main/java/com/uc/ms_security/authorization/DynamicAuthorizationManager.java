package com.uc.ms_security.authorization;

import com.uc.ms_security.service.PermissionService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DynamicAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private static final Pattern USER_PROFILE_PATH =
            Pattern.compile("^/api/users/(\\d+)/profile$");

    private final PermissionService permissionService;

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        final long userId;
        try {
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            return new AuthorizationDecision(false);
        }

        Matcher profilePath = USER_PROFILE_PATH.matcher(
                context.getRequest().getRequestURI());
        if (profilePath.matches()
                && Long.toString(userId).equals(profilePath.group(1))
                && isSelfServiceProfileMethod(
                        context.getRequest().getMethod())) {
            return new AuthorizationDecision(true);
        }

        boolean permitted = permissionService.hasPermission(
                userId,
                context.getRequest().getMethod(),
                context.getRequest().getRequestURI());
        return new AuthorizationDecision(permitted);
    }

    private boolean isSelfServiceProfileMethod(String method) {
        return "GET".equals(method)
                || "POST".equals(method)
                || "PUT".equals(method);
    }
}
