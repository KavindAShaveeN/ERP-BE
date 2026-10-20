package com.rr.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Request / response shapes for the role & permission admin API. */
public final class RbacDtos {

    private RbacDtos() {
    }

    public record PermissionDto(String code, String module, String action, String description) {
    }

    public record RoleDto(Integer roleId, String name, String description, boolean system, boolean active,
                          List<String> permissionCodes, int userCount) {
    }

    public record RoleRequest(@NotBlank @Size(max = 100) String name,
                              @Size(max = 300) String description,
                              Boolean active,
                              List<String> permissionCodes) {
    }

    /** projectCode null = the role applies in every project. */
    public record RoleAssignment(Integer roleId, String roleName, String projectCode) {
    }

    public record PermissionOverride(String permissionCode, String projectCode, boolean allow) {
    }

    public record UserAccessDto(String employeeCode, List<RoleAssignment> roles, List<PermissionOverride> overrides) {
    }

    public record UserAccessRequest(List<RoleAssignment> roles, List<PermissionOverride> overrides) {
    }

    public record MeDto(String employeeCode, String projectCode, List<String> roles, List<String> permissions) {
    }

    /** PROJECT.* permissions the caller holds for one project. */
    public record ProjectAccessEntry(String projectCode, List<String> permissions) {
    }

    public record ProjectAccessDto(boolean canCreate, List<ProjectAccessEntry> projects) {
    }
}
