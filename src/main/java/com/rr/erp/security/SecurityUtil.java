package com.rr.erp.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** The logged-in employee, taken from the verified token - never from the request body. */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /** Employee code of the caller, or null if the request is not authenticated. */
    public static String currentEmployeeCodeOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof String code)
                || "anonymousUser".equals(code)) {
            return null;
        }
        return code;
    }

    public static String currentEmployeeCode() {
        String code = currentEmployeeCodeOrNull();
        if (code == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return code;
    }
}
