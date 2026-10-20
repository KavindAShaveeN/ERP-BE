package com.rr.erp.service;

import com.rr.erp.dto.AssignedProject;
import com.rr.erp.dto.ChangePasswordRequest;
import com.rr.erp.dto.LoginRequest;
import com.rr.erp.dto.LoginResponse;
import com.rr.erp.dto.RbacDtos.RoleAssignment;
import com.rr.erp.dto.RbacDtos.RoleDto;
import com.rr.erp.dto.RegisterCredentialRequest;
import com.rr.erp.dto.UserLoginDto;
import com.rr.erp.entity.Credential;
import com.rr.erp.repository.LoginRepository;
import com.rr.erp.repository.RbacRepository;
import com.rr.erp.security.JwtService;
import com.rr.erp.security.SecurityUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final LoginHistoryService loginHistoryService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final RbacRepository rbacRepository;

    public LoginService(LoginRepository loginRepository, LoginHistoryService loginHistoryService,
                        JwtService jwtService, RbacRepository rbacRepository) {
        this.loginRepository = loginRepository;
        this.loginHistoryService = loginHistoryService;
        this.jwtService = jwtService;
        this.rbacRepository = rbacRepository;
    }


    /** Every employee and whether they already have a login (for the administration page). */
    public List<UserLoginDto> listEmployeeLogins() {
        return loginRepository.findEmployeeLogins();
    }


    // Every attempt — successful or not — is recorded in login_history (see LoginHistoryService).
    public Optional<LoginResponse> login(LoginRequest loginRequest, String ipAddress, String userAgent) {

        Optional<Credential> credential =
                loginRepository.findByUserName(loginRequest.getUserName());

        if (credential.isEmpty()) {
            loginHistoryService.recordLogin(
                    loginRequest.getUserName(), null,
                    LoginHistoryService.STATUS_FAILED_UNKNOWN_USER, ipAddress, userAgent
            );
            return Optional.empty();
        }

        String employeeCode =
                credential.get().getEmployeeCode();

        if (!passwordEncoder.matches(loginRequest.getPassword(), credential.get().getPassword())) {
            loginHistoryService.recordLogin(
                    loginRequest.getUserName(), employeeCode,
                    LoginHistoryService.STATUS_FAILED_BAD_PASSWORD, ipAddress, userAgent
            );
            return Optional.empty();
        }

        List<AssignedProject> assignedProjects =
                loginRepository.getAssignedProjects(employeeCode);
        String employeeName = loginRepository.getEmployeeName(employeeCode);

        UUID sessionId = loginHistoryService.recordLogin(
                loginRequest.getUserName(), employeeCode,
                LoginHistoryService.STATUS_SUCCESS, ipAddress, userAgent
        );

        LoginResponse response =
                new LoginResponse(
                        employeeCode,
                        employeeName,
                        assignedProjects,
                        sessionId,
                        loginRepository.isCashManager(employeeCode),
                        jwtService.issue(employeeCode)
                );

        return Optional.of(response);
    }


    public boolean changePassword(ChangePasswordRequest changePasswordRequest) {

        Optional<Credential> credential =
                loginRepository.findByUserName(changePasswordRequest.getUserName());

        if (credential.isEmpty() ||
                !passwordEncoder.matches(changePasswordRequest.getOldPassword(), credential.get().getPassword())) {
            return false;
        }

        int updatedRows =
                loginRepository.updatePassword(
                        changePasswordRequest.getUserName(),
                        passwordEncoder.encode(changePasswordRequest.getNewPassword())
                );

        return updatedRows > 0;
    }


    /** Creates the login and gives the employee their first role, together or not at all. */
    @Transactional
    public RegisterResult register(RegisterCredentialRequest registerRequest) {

        if (!loginRepository.employeeExists(registerRequest.getEmployeeCode())) {
            return RegisterResult.EMPLOYEE_NOT_FOUND;
        }

        RoleDto role = rbacRepository.findRole(registerRequest.getRoleId()).filter(RoleDto::active).orElse(null);
        if (role == null) {
            return RegisterResult.ROLE_NOT_FOUND;
        }

        if (loginRepository.employeeHasCredential(registerRequest.getEmployeeCode())) {
            return RegisterResult.EMPLOYEE_ALREADY_HAS_LOGIN;
        }

        if (loginRepository.userNameTaken(registerRequest.getUserName())) {
            return RegisterResult.USERNAME_TAKEN;
        }

        loginRepository.insertCredential(
                registerRequest.getEmployeeCode(),
                registerRequest.getUserName(),
                passwordEncoder.encode(registerRequest.getPassword())
        );
        String projectCode = registerRequest.getProjectCode() == null || registerRequest.getProjectCode().isBlank()
                ? null : registerRequest.getProjectCode().trim();
        rbacRepository.replaceUserRoles(registerRequest.getEmployeeCode(),
                List.of(new RoleAssignment(role.roleId(), role.name(), projectCode)));
        rbacRepository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "USER", registerRequest.getEmployeeCode(),
                "created login '" + registerRequest.getUserName() + "' with role " + role.name()
                        + (projectCode == null ? " in every project" : " in project " + projectCode));

        return RegisterResult.SUCCESS;
    }
}