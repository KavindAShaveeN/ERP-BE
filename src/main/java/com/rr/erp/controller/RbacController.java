package com.rr.erp.controller;

import com.rr.erp.dto.RbacDtos.MeDto;
import com.rr.erp.dto.RbacDtos.PermissionDto;
import com.rr.erp.dto.RbacDtos.ProjectAccessDto;
import com.rr.erp.dto.RbacDtos.RoleDto;
import com.rr.erp.dto.RbacDtos.RoleRequest;
import com.rr.erp.dto.RbacDtos.UserAccessDto;
import com.rr.erp.dto.RbacDtos.UserAccessRequest;
import com.rr.erp.service.RbacService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Roles, the permission catalogue, per-user role assignments and the caller's own access. */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class RbacController {

    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }


    // GET /api/me?projectCode=...  - the logged-in user's roles and effective permissions
    @GetMapping("/me")
    public MeDto me(@RequestParam(required = false) String projectCode) {
        return rbacService.me(projectCode);
    }

    // GET /api/me/projects - per project, the PROJECT.* permissions the logged-in user holds
    @GetMapping("/me/projects")
    public ProjectAccessDto myProjects() {
        return rbacService.projectAccess();
    }

    @GetMapping("/permissions")
    @PreAuthorize("@perm.has('ROLE.MANAGE') or @perm.has('USER.MANAGE')")
    public List<PermissionDto> permissions() {
        return rbacService.listPermissions();
    }

    @GetMapping("/roles")
    @PreAuthorize("@perm.has('ROLE.MANAGE') or @perm.has('USER.MANAGE') or @perm.has('WORKFLOW.MANAGE')")
    public List<RoleDto> roles() {
        return rbacService.listRoles();
    }

    @PostMapping("/roles")
    @PreAuthorize("@perm.has('ROLE.MANAGE')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createRole(request));
    }

    @PutMapping("/roles/{roleId}")
    @PreAuthorize("@perm.has('ROLE.MANAGE')")
    public RoleDto updateRole(@PathVariable int roleId, @Valid @RequestBody RoleRequest request) {
        return rbacService.updateRole(roleId, request);
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("@perm.has('ROLE.MANAGE')")
    public ResponseEntity<Void> deleteRole(@PathVariable int roleId) {
        rbacService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{employeeCode}/access")
    @PreAuthorize("@perm.has('USER.MANAGE')")
    public UserAccessDto userAccess(@PathVariable String employeeCode) {
        return rbacService.userAccess(employeeCode);
    }

    // PUT replaces the employee's whole set of role assignments and overrides
    @PutMapping("/users/{employeeCode}/access")
    @PreAuthorize("@perm.has('USER.MANAGE')")
    public UserAccessDto replaceUserAccess(@PathVariable String employeeCode, @RequestBody UserAccessRequest request) {
        return rbacService.replaceUserAccess(employeeCode, request);
    }
}
