package com.rr.erp.repository;


import com.rr.erp.entity.EmployeeStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmployeeStatus> getAllEmployeeStatuses() {

        String sql = """
                SELECT
                    employeeStatusId AS "employeeStatusId",
                    employeeStatusName AS "statusName"
                FROM employeeStatus
                ORDER BY employeeStatusName
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(EmployeeStatus.class)
        );
    }
}