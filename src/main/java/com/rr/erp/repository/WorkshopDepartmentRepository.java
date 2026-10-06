package com.rr.erp.repository;

import com.rr.erp.entity.WorkshopDepartment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkshopDepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<WorkshopDepartment> getAllWorkshopDepartments() {

        String sql = """
                SELECT
                    workshop_department_id AS "workshopDepartmentId",
                    workshop_department_name AS "workshopDepartmentName"
                FROM workshop_department
                ORDER BY workshop_department_name
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    WorkshopDepartment workshopDepartment = new WorkshopDepartment();

                    workshopDepartment.setWorkshopDepartmentId(
                            rs.getInt("workshopDepartmentId")
                    );

                    workshopDepartment.setWorkshopDepartmentName(
                            rs.getString("workshopDepartmentName")
                    );

                    return workshopDepartment;
                }
        );
    }
}
