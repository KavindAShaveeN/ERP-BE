package com.rr.erp.service;

import com.rr.erp.dto.ProjectResponseDTO;
import com.rr.erp.dto.RbacDtos.MeDto;
import com.rr.erp.dto.RbacDtos.ProjectAccessDto;
import com.rr.erp.dto.RbacDtos.ProjectAccessEntry;
import com.rr.erp.dto.RbacDtos.PermissionDto;
import com.rr.erp.dto.RbacDtos.PermissionOverride;
import com.rr.erp.dto.RbacDtos.RoleAssignment;
import com.rr.erp.dto.RbacDtos.RoleDto;
import com.rr.erp.dto.RbacDtos.RoleRequest;
import com.rr.erp.dto.RbacDtos.UserAccessDto;
import com.rr.erp.dto.RbacDtos.UserAccessRequest;
import com.rr.erp.repository.ProjectRepository;
import com.rr.erp.repository.RbacRepository;
import com.rr.erp.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RbacService {

    private final RbacRepository repository;
    private final PermissionService permissionService;

    private final ProjectRepository projectRepository;

    public RbacService(RbacRepository repository, PermissionService permissionService,
                       ProjectRepository projectRepository) {
        this.repository = repository;
        this.permissionService = permissionService;
        this.projectRepository = projectRepository;
    }


    public List<PermissionDto> listPermissions() {
        return repository.findAllPermissions();
    }

    public List<RoleDto> listRoles() {
        return repository.findAllRoles();
    }

    public MeDto me(String projectCode) {
        String employeeCode = SecurityUtil.currentEmployeeCode();
        boolean anyScope = projectCode == null || projectCode.isBlank();
        List<String> permissions = new ArrayList<>(permissionService.permissionsFor(employeeCode, projectCode, anyScope));
        permissions.sort(String::compareTo);
        return new MeDto(employeeCode, projectCode, permissionService.roleNamesFor(employeeCode, projectCode), permissions);
    }


    /** Which projects the caller may view / edit (a project-scoped role or a global role), and whether they may create one. */
    public ProjectAccessDto projectAccess() {
        String employeeCode = SecurityUtil.currentEmployeeCode();
        boolean canCreate = permissionService.permissionsFor(employeeCode, null, true).contains("PROJECT.CREATE");

        List<ProjectAccessEntry> projects = new ArrayList<>();
        for (ProjectResponseDTO project : projectRepository.getAllProjects()) {
            List<String> codes = permissionService.permissionsFor(employeeCode, project.getProjectCode(), false).stream()
                    .filter(code -> code.startsWith("PROJECT."))
                    .sorted()
                    .toList();
            if (!codes.isEmpty()) {
                projects.add(new ProjectAccessEntry(project.getProjectCode(), codes));
            }
        }
        return new ProjectAccessDto(canCreate, projects);
    }


    @Transactional
    public RoleDto createRole(RoleRequest request) {
        String name = request.name().trim();
        if (repository.roleNameExists(name, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A role named '" + name + "' already exists");
        }
        List<String> codes = validCodes(request.permissionCodes());
        int roleId = repository.insertRole(name, request.description());
        repository.replaceRolePermissions(roleId, codes);
        repository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "ROLE", String.valueOf(roleId),
                "created '" + name + "' with " + codes.size() + " permissions");
        permissionService.evictAll();
        return repository.findRole(roleId).orElseThrow();
    }

    @Transactional
    public RoleDto updateRole(int roleId, RoleRequest request) {
        RoleDto existing = repository.findRole(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        String name = request.name().trim();
        if (repository.roleNameExists(name, roleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A role named '" + name + "' already exists");
        }
        boolean isAdminRole = "ADMIN".equals(existing.name());
        if (isAdminRole && (!"ADMIN".equals(name) || Boolean.FALSE.equals(request.active()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The ADMIN role cannot be renamed or deactivated");
        }
        List<String> codes = validCodes(request.permissionCodes());
        if (isAdminRole && !new HashSet<>(codes).containsAll(repository.findAllPermissionCodes())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The ADMIN role must keep every permission");
        }
        repository.updateRole(roleId, name, request.description(),
                request.active() == null ? existing.active() : request.active());
        repository.replaceRolePermissions(roleId, codes);
        repository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "ROLE", String.valueOf(roleId),
                "updated '" + name + "' to " + codes.size() + " permissions");
        permissionService.evictAll();
        return repository.findRole(roleId).orElseThrow();
    }

    @Transactional
    public void deleteRole(int roleId) {
        RoleDto existing = repository.findRole(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        if (existing.system()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "System roles can be edited but not deleted");
        }
        if (repository.countRoleUsage(roleId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This role is assigned to users or used in an approval workflow; remove it there first");
        }
        repository.deleteRole(roleId);
        repository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "ROLE", String.valueOf(roleId), "deleted '" + existing.name() + "'");
        permissionService.evictAll();
    }


    public UserAccessDto userAccess(String employeeCode) {
        requireEmployee(employeeCode);
        return new UserAccessDto(employeeCode, repository.findUserRoles(employeeCode), repository.findUserOverrides(employeeCode));
    }

    @Transactional
    public UserAccessDto replaceUserAccess(String employeeCode, UserAccessRequest request) {
        requireEmployee(employeeCode);
        List<RoleAssignment> roles = request.roles() == null ? List.of() : request.roles();
        List<PermissionOverride> overrides = request.overrides() == null ? List.of() : request.overrides();

        Set<String> known = new HashSet<>(repository.findAllPermissionCodes());
        for (PermissionOverride o : overrides) {
            if (!known.contains(o.permissionCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown permission: " + o.permissionCode());
            }
        }

        repository.replaceUserRoles(employeeCode, roles);
        repository.replaceUserOverrides(employeeCode, overrides);

        if (repository.countGlobalAdmins() == 0) {
            // Throwing rolls the whole change back.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one employee must keep the global ADMIN role");
        }
        repository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "USER_ACCESS", employeeCode,
                roles.size() + " role assignments, " + overrides.size() + " overrides");
        permissionService.evictAll();
        return userAccess(employeeCode);
    }


    private List<String> validCodes(List<String> codes) {
        List<String> requested = codes == null ? List.of() : codes;
        Set<String> known = new HashSet<>(repository.findAllPermissionCodes());
        for (String code : requested) {
            if (!known.contains(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown permission: " + code);
            }
        }
        return requested;
    }

    private void requireEmployee(String employeeCode) {
        if (!repository.employeeExists(employeeCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
    }
}
