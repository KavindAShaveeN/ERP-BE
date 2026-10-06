package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.LoginHistory;
import com.rr.erp.repository.LoginHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LoginHistoryService {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED_BAD_PASSWORD = "FAILED_BAD_PASSWORD";
    public static final String STATUS_FAILED_UNKNOWN_USER = "FAILED_UNKNOWN_USER";

    private static final Set<String> STATUSES =
            Set.of(STATUS_SUCCESS, STATUS_FAILED_BAD_PASSWORD, STATUS_FAILED_UNKNOWN_USER);

    // Column sizes in login_history — longer values are cut rather than failing the insert.
    private static final int MAX_USER_NAME = 100;
    private static final int MAX_IP_ADDRESS = 45;
    private static final int MAX_USER_AGENT = 500;

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryService.class);

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    /**
     * Records one login attempt and returns its id (the sessionId handed back on a successful
     * login). Never throws — a failure to write the history is logged and returns null, so a
     * logging problem can never stop someone from logging in.
     */
    public UUID recordLogin(
            String userName,
            String employeeCode,
            String loginStatus,
            String ipAddress,
            String userAgent
    ) {

        try {
            LoginHistory history = new LoginHistory();
            history.setLoginHistoryId(UUID.randomUUID());
            history.setUserName(truncate(userName, MAX_USER_NAME));
            history.setEmployeeCode(employeeCode);
            history.setLoginStatus(loginStatus);
            history.setLoginTime(LocalDateTime.now());
            history.setIpAddress(truncate(ipAddress, MAX_IP_ADDRESS));
            history.setUserAgent(truncate(userAgent, MAX_USER_AGENT));

            loginHistoryRepository.insert(history);

            return history.getLoginHistoryId();

        } catch (RuntimeException e) {
            log.error("Could not record login history for user '{}'", userName, e);
            return null;
        }
    }

    /** Marks the session as logged out. Calling it again for the same session changes nothing. */
    public void recordLogout(UUID sessionId) {

        if (sessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required");
        }

        if (!loginHistoryRepository.existsSuccessfulLogin(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Login session not found: " + sessionId);
        }

        loginHistoryRepository.markLogout(sessionId, LocalDateTime.now());
    }

    public PagedResponse<LoginHistory> getLoginHistory(
            String employeeCode,
            String loginStatus,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        String status = blankToNull(loginStatus);
        if (status != null) {
            status = status.toUpperCase();
            if (!STATUSES.contains(status)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "loginStatus must be one of " + STATUSES
                );
            }
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        String employee = blankToNull(employeeCode);

        List<LoginHistory> content = loginHistoryRepository.findPage(
                employee, status, fromDate, toDate, safePage, safeSize
        );
        long total = loginHistoryRepository.count(employee, status, fromDate, toDate);

        return new PagedResponse<>(content, safePage, safeSize, total);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
