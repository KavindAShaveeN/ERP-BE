package com.rr.erp.controller;

import com.rr.erp.dto.ChangePasswordRequest;
import com.rr.erp.dto.LoginRequest;
import com.rr.erp.dto.LoginResponse;
import com.rr.erp.dto.LogoutRequest;
import com.rr.erp.dto.RegisterCredentialRequest;
import com.rr.erp.dto.UserLoginDto;
import com.rr.erp.service.LoginHistoryService;
import com.rr.erp.service.LoginService;
import com.rr.erp.service.RegisterResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final LoginService loginService;
    private final LoginHistoryService loginHistoryService;

    public LoginController(LoginService loginService, LoginHistoryService loginHistoryService) {
        this.loginService = loginService;
        this.loginHistoryService = loginHistoryService;
    }


    // POST /api/login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {

        if (loginRequest.getUserName() == null ||
                loginRequest.getUserName().isBlank() ||
                loginRequest.getPassword() == null ||
                loginRequest.getPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Username and password are required"
                            )
                    );
        }


        Optional<LoginResponse> loginResponse =
                loginService.login(
                        loginRequest,
                        clientIpAddress(request),
                        request.getHeader(HttpHeaders.USER_AGENT)
                );


        if (loginResponse.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid username or password"
                            )
                    );
        }


        return ResponseEntity.ok(
                loginResponse.get()
        );
    }


    // POST /api/logout  { "sessionId": "<sessionId from the login response>" }
    // Records the logout time on that login's history row. 204 on success (also when it was
    // already logged out), 404 for an unknown sessionId.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequest logoutRequest
    ) {

        loginHistoryService.recordLogout(logoutRequest.getSessionId());

        return ResponseEntity.noContent().build();
    }


    // The caller's IP. Behind a proxy / load balancer the real client is the first address in
    // X-Forwarded-For; otherwise it is the direct connection's address.
    private static String clientIpAddress(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }


    // POST /api/change-password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) {

        if (changePasswordRequest.getUserName() == null ||
                changePasswordRequest.getUserName().isBlank() ||
                changePasswordRequest.getOldPassword() == null ||
                changePasswordRequest.getOldPassword().isBlank() ||
                changePasswordRequest.getNewPassword() == null ||
                changePasswordRequest.getNewPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Username, old password and new password are required"
                            )
                    );
        }

        if (changePasswordRequest.getNewPassword().equals(
                changePasswordRequest.getOldPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "New password must be different from old password"
                            )
                    );
        }

        boolean changed = loginService.changePassword(changePasswordRequest);

        if (!changed) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid username or old password"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password changed successfully"
                )
        );
    }


    // GET /api/user-logins - every employee and the username of their login, if any (administrators only)
    @GetMapping("/user-logins")
    @PreAuthorize("@perm.has('USER.MANAGE')")
    public List<UserLoginDto> userLogins() {
        return loginService.listEmployeeLogins();
    }


    // POST /api/register - creates a login for an existing employee (administrators only)
    @PostMapping("/register")
    @PreAuthorize("@perm.has('USER.MANAGE')")
    public ResponseEntity<?> register(
            @RequestBody RegisterCredentialRequest registerRequest
    ) {

        if (registerRequest.getEmployeeCode() == null ||
                registerRequest.getEmployeeCode().isBlank() ||
                registerRequest.getUserName() == null ||
                registerRequest.getUserName().isBlank() ||
                registerRequest.getPassword() == null ||
                registerRequest.getPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Employee code, username and password are required"
                            )
                    );
        }

        if (registerRequest.getPassword().length() < MIN_PASSWORD_LENGTH) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "The password must be at least " + MIN_PASSWORD_LENGTH + " characters"));
        }
        registerRequest.setEmployeeCode(registerRequest.getEmployeeCode().trim());
        registerRequest.setUserName(registerRequest.getUserName().trim());

        RegisterResult result = loginService.register(registerRequest);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(
                    Map.of("message", "Login created successfully")
            );
            case EMPLOYEE_NOT_FOUND -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No employee found with that employee code"));
            case EMPLOYEE_ALREADY_HAS_LOGIN -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This employee already has a login"));
            case USERNAME_TAKEN -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This username is already taken"));
        };
    }
}