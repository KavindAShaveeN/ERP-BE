package com.rr.erp.service;

import com.rr.erp.dto.AssignedProject;
import com.rr.erp.dto.ChangePasswordRequest;
import com.rr.erp.dto.LoginRequest;
import com.rr.erp.dto.LoginResponse;
import com.rr.erp.dto.RegisterCredentialRequest;
import com.rr.erp.entity.Credential;
import com.rr.erp.repository.LoginRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final LoginHistoryService loginHistoryService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(LoginRepository loginRepository, LoginHistoryService loginHistoryService) {
        this.loginRepository = loginRepository;
        this.loginHistoryService = loginHistoryService;
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
                        sessionId
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


    public RegisterResult register(RegisterCredentialRequest registerRequest) {

        if (!loginRepository.employeeExists(registerRequest.getEmployeeCode())) {
            return RegisterResult.EMPLOYEE_NOT_FOUND;
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

        return RegisterResult.SUCCESS;
    }
}