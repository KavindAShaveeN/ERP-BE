package com.rr.erp.service;

import com.rr.erp.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Answers "may this employee do X (in this project)?" from the roles an Admin has configured.
 * Registered under the bean name "perm" so endpoints can use
 * {@code @PreAuthorize("@perm.has('ROLE.MANAGE')")}.
 *
 * Rules: a permission comes from any of the employee's roles (global, or for that project) plus
 * allow-overrides; a deny-override always wins.
 */
@Service("perm")
public class PermissionService {

    private static final long CACHE_MILLIS = 15_000;

    private record Scoped(String code, String projectCode) {
    }

    private record Rules(List<Scoped> granted, List<Scoped> allowed, List<Scoped> denied, long loadedAt) {
    }

    private final JdbcTemplate jdbcTemplate;
    private final boolean enforce;
    private final Map<String, Rules> cache = new ConcurrentHashMap<>();

    public PermissionService(JdbcTemplate jdbcTemplate, @Value("${security.enforce:true}") boolean enforce) {
        this.jdbcTemplate = jdbcTemplate;
        this.enforce = enforce;
    }


    /** Does the caller hold this permission in any scope? Use for menus and admin endpoints. */
    public boolean has(String code) {
        return hasFor(SecurityUtil.currentEmployeeCodeOrNull(), code, null, true);
    }

    /** Does the caller hold this permission for the given project? */
    public boolean has(String code, String projectCode) {
        return hasFor(SecurityUtil.currentEmployeeCodeOrNull(), code, projectCode, false);
    }

    public boolean hasFor(String employeeCode, String code, String projectCode, boolean anyScope) {
        if (!enforce) {
            return true;
        }
        if (employeeCode == null) {
            return false;
        }
        return permissionsFor(employeeCode, projectCode, anyScope).contains(code);
    }

    /** Throws 403 unless the caller holds the permission for the project. */
    public void require(String code, String projectCode) {
        if (!has(code, projectCode)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission: " + code);
        }
    }

    /** Effective permission codes for the employee in a project (global roles always count). */
    public Set<String> permissionsFor(String employeeCode, String projectCode, boolean anyScope) {
        Rules rules = load(employeeCode);

        Set<String> result = new HashSet<>();
        for (Scoped s : rules.granted()) {
            if (applies(s, projectCode, anyScope)) {
                result.add(s.code());
            }
        }
        for (Scoped s : rules.allowed()) {
            if (applies(s, projectCode, anyScope)) {
                result.add(s.code());
            }
        }
        for (Scoped s : rules.denied()) {
            if (applies(s, projectCode, anyScope)) {
                result.remove(s.code());
            }
        }
        return result;
    }

    public List<String> roleNamesFor(String employeeCode, String projectCode) {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT r.name
                FROM user_roles ur JOIN roles r ON r.role_id = ur.role_id
                WHERE ur.employee_code = ? AND r.is_active
                  AND (ur.project_code IS NULL OR ur.project_code = ?)
                ORDER BY r.name
                """, String.class, employeeCode, projectCode);
    }

    /** Call after any role / permission / assignment change. */
    public void evictAll() {
        cache.clear();
    }


    private static boolean applies(Scoped s, String projectCode, boolean anyScope) {
        return anyScope || s.projectCode() == null || s.projectCode().equals(projectCode);
    }

    private Rules load(String employeeCode) {
        Rules cached = cache.get(employeeCode);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.loadedAt() < CACHE_MILLIS) {
            return cached;
        }

        List<Scoped> granted = jdbcTemplate.query("""
                SELECT p.code, ur.project_code
                FROM user_roles ur
                JOIN roles r ON r.role_id = ur.role_id AND r.is_active
                JOIN role_permissions rp ON rp.role_id = r.role_id
                JOIN permissions p ON p.permission_id = rp.permission_id
                WHERE ur.employee_code = ?
                """, (rs, i) -> new Scoped(rs.getString(1), rs.getString(2)), employeeCode);

        List<Scoped> allowed = overrides(employeeCode, true);
        List<Scoped> denied = overrides(employeeCode, false);

        Rules rules = new Rules(granted, allowed, denied, now);
        cache.put(employeeCode, rules);
        return rules;
    }

    private List<Scoped> overrides(String employeeCode, boolean allow) {
        return jdbcTemplate.query("""
                SELECT p.code, o.project_code
                FROM user_permission_overrides o JOIN permissions p ON p.permission_id = o.permission_id
                WHERE o.employee_code = ? AND o.allow = ?
                """, (rs, i) -> new Scoped(rs.getString(1), rs.getString(2)), employeeCode, allow);
    }
}
