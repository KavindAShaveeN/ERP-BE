package com.rr.erp.repository;

import com.rr.erp.dto.RbacDtos.PermissionDto;
import com.rr.erp.dto.RbacDtos.PermissionOverride;
import com.rr.erp.dto.RbacDtos.RoleAssignment;
import com.rr.erp.dto.RbacDtos.RoleDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RbacRepository {

    private final JdbcTemplate jdbc;

    public RbacRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public List<PermissionDto> findAllPermissions() {
        return jdbc.query("""
                SELECT code, module, action, description FROM permissions ORDER BY module, action
                """, (rs, i) -> new PermissionDto(
                rs.getString("code"), rs.getString("module"), rs.getString("action"), rs.getString("description")));
    }

    public List<String> findAllPermissionCodes() {
        return jdbc.queryForList("SELECT code FROM permissions", String.class);
    }


    // ---- roles ----

    public List<RoleDto> findAllRoles() {
        Map<Integer, List<String>> permsByRole = new HashMap<>();
        jdbc.query("""
                SELECT rp.role_id, p.code FROM role_permissions rp
                JOIN permissions p ON p.permission_id = rp.permission_id ORDER BY p.code
                """, rs -> {
            permsByRole.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>()).add(rs.getString(2));
        });

        return jdbc.query("""
                SELECT r.role_id, r.name, r.description, r.is_system, r.is_active,
                       (SELECT COUNT(DISTINCT ur.employee_code) FROM user_roles ur WHERE ur.role_id = r.role_id) AS user_count
                FROM roles r ORDER BY r.name
                """, (rs, i) -> new RoleDto(
                rs.getInt("role_id"), rs.getString("name"), rs.getString("description"),
                rs.getBoolean("is_system"), rs.getBoolean("is_active"),
                permsByRole.getOrDefault(rs.getInt("role_id"), List.of()), rs.getInt("user_count")));
    }

    public Optional<RoleDto> findRole(int roleId) {
        return findAllRoles().stream().filter(r -> r.roleId() == roleId).findFirst();
    }

    public boolean roleNameExists(String name, Integer exceptRoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM roles WHERE LOWER(name) = LOWER(?) AND (?::int IS NULL OR role_id <> ?::int)",
                Integer.class, name, exceptRoleId, exceptRoleId);
        return n != null && n > 0;
    }

    public int insertRole(String name, String description) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO roles (name, description, is_system, is_active) VALUES (?, ?, false, true)
                RETURNING role_id
                """, Integer.class, name, description);
        return id == null ? 0 : id;
    }

    public void updateRole(int roleId, String name, String description, boolean active) {
        jdbc.update("UPDATE roles SET name = ?, description = ?, is_active = ? WHERE role_id = ?",
                name, description, active, roleId);
    }

    public int deleteRole(int roleId) {
        return jdbc.update("DELETE FROM roles WHERE role_id = ?", roleId);
    }

    public int countRoleUsage(int roleId) {
        Integer n = jdbc.queryForObject("""
                SELECT (SELECT COUNT(*) FROM user_roles WHERE role_id = ?)
                     + (SELECT COUNT(*) FROM approval_steps WHERE role_id = ?)
                """, Integer.class, roleId, roleId);
        return n == null ? 0 : n;
    }

    public void replaceRolePermissions(int roleId, List<String> codes) {
        jdbc.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        for (String code : codes) {
            jdbc.update("""
                    INSERT INTO role_permissions (role_id, permission_id)
                    SELECT ?, permission_id FROM permissions WHERE code = ?
                    ON CONFLICT DO NOTHING
                    """, roleId, code);
        }
    }


    // ---- user access ----

    public List<RoleAssignment> findUserRoles(String employeeCode) {
        return jdbc.query("""
                SELECT r.role_id, r.name, ur.project_code
                FROM user_roles ur JOIN roles r ON r.role_id = ur.role_id
                WHERE ur.employee_code = ? ORDER BY r.name, ur.project_code
                """, (rs, i) -> new RoleAssignment(rs.getInt(1), rs.getString(2), rs.getString(3)), employeeCode);
    }

    public List<PermissionOverride> findUserOverrides(String employeeCode) {
        return jdbc.query("""
                SELECT p.code, o.project_code, o.allow
                FROM user_permission_overrides o JOIN permissions p ON p.permission_id = o.permission_id
                WHERE o.employee_code = ? ORDER BY p.code
                """, (rs, i) -> new PermissionOverride(rs.getString(1), rs.getString(2), rs.getBoolean(3)), employeeCode);
    }

    public void replaceUserRoles(String employeeCode, List<RoleAssignment> roles) {
        jdbc.update("DELETE FROM user_roles WHERE employee_code = ?", employeeCode);
        for (RoleAssignment a : roles) {
            jdbc.update("""
                    INSERT INTO user_roles (employee_code, role_id, project_code) VALUES (?, ?, ?)
                    ON CONFLICT DO NOTHING
                    """, employeeCode, a.roleId(), blankToNull(a.projectCode()));
        }
    }

    public void replaceUserOverrides(String employeeCode, List<PermissionOverride> overrides) {
        jdbc.update("DELETE FROM user_permission_overrides WHERE employee_code = ?", employeeCode);
        for (PermissionOverride o : overrides) {
            jdbc.update("""
                    INSERT INTO user_permission_overrides (employee_code, permission_id, project_code, allow)
                    SELECT ?, permission_id, ?, ? FROM permissions WHERE code = ?
                    ON CONFLICT DO NOTHING
                    """, employeeCode, blankToNull(o.projectCode()), o.allow(), o.permissionCode());
        }
    }

    /** How many distinct employees still hold ADMIN globally. */
    public int countGlobalAdmins() {
        Integer n = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT ur.employee_code) FROM user_roles ur
                JOIN roles r ON r.role_id = ur.role_id
                WHERE r.name = 'ADMIN' AND r.is_active AND ur.project_code IS NULL
                """, Integer.class);
        return n == null ? 0 : n;
    }

    public boolean employeeExists(String employeeCode) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM employee WHERE employee_code = ?", Integer.class, employeeCode);
        return n != null && n > 0;
    }

    public void audit(String actor, String entity, String entityId, String change) {
        jdbc.update("INSERT INTO rbac_audit_log (actor_code, entity, entity_id, change) VALUES (?, ?, ?, ?)",
                actor, entity, entityId, change);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
