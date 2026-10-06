package com.rr.erp.repository;

import com.rr.erp.entity.LoginHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class LoginHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public LoginHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<LoginHistory> LOGIN_HISTORY_ROW_MAPPER = (rs, rowNum) -> {

        LoginHistory row = new LoginHistory();

        row.setLoginHistoryId(rs.getObject("login_history_id", UUID.class));
        row.setUserName(rs.getString("user_name"));
        row.setEmployeeCode(rs.getString("employee_code"));
        row.setEmployeeName(rs.getString("full_name"));
        row.setLoginStatus(rs.getString("login_status"));

        Timestamp loginTime = rs.getTimestamp("login_time");
        row.setLoginTime(loginTime != null ? loginTime.toLocalDateTime() : null);

        Timestamp logoutTime = rs.getTimestamp("logout_time");
        row.setLogoutTime(logoutTime != null ? logoutTime.toLocalDateTime() : null);

        row.setIpAddress(rs.getString("ip_address"));
        row.setUserAgent(rs.getString("user_agent"));

        return row;
    };

    public void insert(LoginHistory history) {

        String sql = """
                INSERT INTO login_history (
                    login_history_id,
                    user_name,
                    employee_code,
                    login_status,
                    login_time,
                    ip_address,
                    user_agent
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                history.getLoginHistoryId(),
                history.getUserName(),
                history.getEmployeeCode(),
                history.getLoginStatus(),
                history.getLoginTime(),
                history.getIpAddress(),
                history.getUserAgent()
        );
    }

    public boolean existsSuccessfulLogin(UUID loginHistoryId) {

        String sql = """
                SELECT COUNT(*)
                FROM login_history
                WHERE login_history_id = ?
                  AND login_status = 'SUCCESS'
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginHistoryId);

        return count != null && count > 0;
    }

    /** Sets the logout time once — a repeated logout call for the same session keeps the first
     * recorded time. */
    public int markLogout(UUID loginHistoryId, LocalDateTime logoutTime) {

        String sql = """
                UPDATE login_history
                SET logout_time = ?
                WHERE login_history_id = ?
                  AND login_status = 'SUCCESS'
                  AND logout_time IS NULL
                """;

        return jdbcTemplate.update(sql, logoutTime, loginHistoryId);
    }

    /** Login history newest first; every filter is optional. toDate is inclusive (whole day). */
    public List<LoginHistory> findPage(
            String employeeCode,
            String loginStatus,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        List<Object> params = new ArrayList<>();
        String where = buildWhere(employeeCode, loginStatus, fromDate, toDate, params);

        String sql = """
                SELECT
                    lh.login_history_id,
                    lh.user_name,
                    lh.employee_code,
                    e.full_name,
                    lh.login_status,
                    lh.login_time,
                    lh.logout_time,
                    lh.ip_address,
                    lh.user_agent
                FROM login_history lh
                LEFT JOIN employee e
                    ON e.employee_code = lh.employee_code
                """ + where + """
                ORDER BY lh.login_time DESC
                LIMIT ? OFFSET ?
                """;

        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql, LOGIN_HISTORY_ROW_MAPPER, params.toArray());
    }

    public long count(
            String employeeCode,
            String loginStatus,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<Object> params = new ArrayList<>();
        String where = buildWhere(employeeCode, loginStatus, fromDate, toDate, params);

        String sql = "SELECT COUNT(*) FROM login_history lh\n" + where;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());

        return count == null ? 0 : count;
    }

    private static String buildWhere(
            String employeeCode,
            String loginStatus,
            LocalDate fromDate,
            LocalDate toDate,
            List<Object> params
    ) {

        List<String> conditions = new ArrayList<>();

        if (employeeCode != null) {
            conditions.add("lh.employee_code = ?");
            params.add(employeeCode);
        }

        if (loginStatus != null) {
            conditions.add("lh.login_status = ?");
            params.add(loginStatus);
        }

        if (fromDate != null) {
            conditions.add("lh.login_time >= ?");
            params.add(fromDate.atStartOfDay());
        }

        if (toDate != null) {
            conditions.add("lh.login_time < ?");
            params.add(toDate.plusDays(1).atStartOfDay());
        }

        return conditions.isEmpty()
                ? ""
                : "WHERE " + String.join(" AND ", conditions) + "\n";
    }
}
