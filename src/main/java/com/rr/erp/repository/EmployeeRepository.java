package com.rr.erp.repository;

import com.rr.erp.dto.EmployeeResponseDTO;
import com.rr.erp.dto.ProjectResponse;
import com.rr.erp.entity.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    public List<EmployeeResponseDTO> getAllEmployees() {

        String sql = """
            SELECT
                e.employee_code AS "employeeCode",
                e.employee_type AS "employeeType",
                e.temporary_y_number AS "temporaryYNumber",
                e.epf_number AS "epfNumber",

                e.employeeStatusId AS "employeeStatusId",
                es.employeeStatusName AS "statusName",

                e.joined_date AS "joinedDate",
                e.permanent_appointment_date AS "permanentAppointmentDate",
                e.department AS "department",

                e.designationId AS "designationId",
                d.designationName AS "designationName",

                e.reporting_supervisor_code AS "reportingSupervisorCode",
                er.full_name AS "reportingSupervisorName",
                e.nic AS "nic",
                e.full_name AS "fullName",
                e.name_with_initials AS "nameWithInitials",
                e.dob AS "dob",
                e.gender AS "gender",
                e.marital_status AS "maritalStatus",
                e.nationality AS "nationality",
                e.contact_number AS "contactNumber",
                e.alt_contact_number AS "altContactNumber",
                e.email AS "email",
                e.residential_address AS "residentialAddress",
                e.emergency_contact_person AS "emergencyContactPerson",
                e.emergency_contact_number AS "emergencyContactNumber",
                e.emergency_contact_relationship AS "emergencyContactRelationship",
                e.bank_name AS "bankName",
                e.bank_branch AS "bankBranch",
                e.bank_account_number AS "bankAccountNumber",
                e.account_holder_name AS "accountHolderName",
                e.basic_salary AS "basicSalary",
                e.profile_photo AS "profilePhoto",
                e.nic_copy AS "nicCopy",
                e.epf_documents AS "epfDocuments",
                e.remarks AS "remarks",
                e.created_at AS "createdAt",
                e.updated_at AS "updatedAt"

            FROM employee e

            LEFT JOIN employeeStatus es
                ON e.employeeStatusId = es.employeeStatusId

            LEFT JOIN designation d
                ON e.designationId = d.designationId
            LEFT JOIN employee er
                ON e.reporting_supervisor_code = er.employee_code
            ORDER BY e.employee_code
            """;

        List<EmployeeResponseDTO> employees =
                jdbcTemplate.query(
                        sql,
                        new BeanPropertyRowMapper<>(EmployeeResponseDTO.class)
                );

        // One query for every employee's projects (instead of one query per employee).
        Map<String, List<ProjectResponse>> projectsByEmployee = new HashMap<>();

        jdbcTemplate.query(
                """
                SELECT
                    ap.employee_code AS "employeeCode",
                    p.projectId AS "projectId",
                    p.project_code AS "projectCode",
                    p.projectName AS "projectName"
                FROM assigned_projects ap
                INNER JOIN project p
                    ON ap.project_id = p.projectId
                ORDER BY p.project_code
                """,
                rs -> {
                    ProjectResponse project = new ProjectResponse();
                    project.setProjectId(rs.getObject("projectId", Integer.class));
                    project.setProjectCode(rs.getString("projectCode"));
                    project.setProjectName(rs.getString("projectName"));

                    projectsByEmployee
                            .computeIfAbsent(rs.getString("employeeCode"), k -> new ArrayList<>())
                            .add(project);
                }
        );

        for (EmployeeResponseDTO employee : employees) {
            employee.setProjectIds(
                    projectsByEmployee.getOrDefault(employee.getEmployeeCode(), new ArrayList<>())
            );
        }

        return employees;
    }


    // =========================================================
    // GET PROJECTS FOR ONE EMPLOYEE
    // =========================================================

    public List<ProjectResponse> getEmployeeProjects(String employeeCode) {

        String sql = """
            SELECT
                p.projectId AS "projectId",
                p.project_code AS "projectCode",
                p.projectName AS "projectName"
            FROM assigned_projects ap
            INNER JOIN project p
                ON ap.project_id = p.projectId
            WHERE ap.employee_code = ?
            ORDER BY p.project_code
            """;

        return jdbcTemplate.query(
                sql,
                new Object[]{employeeCode},
                new BeanPropertyRowMapper<>(ProjectResponse.class)
        );
    }


    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    public int createEmployee(Employee employee) {

        String sql = """
                INSERT INTO employee (
                    employee_code,
                    employee_type,
                    temporary_y_number,
                    epf_number,
                    employeeStatusId,
                    joined_date,
                    permanent_appointment_date,
                    department,
                    designationId,
                    reporting_supervisor_code,
                    nic,
                    full_name,
                    name_with_initials,
                    dob,
                    gender,
                    marital_status,
                    nationality,
                    contact_number,
                    alt_contact_number,
                    email,
                    residential_address,
                    emergency_contact_person,
                    emergency_contact_number,
                    emergency_contact_relationship,
                    bank_name,
                    bank_branch,
                    bank_account_number,
                    account_holder_name,
                    basic_salary,
                    profile_photo,
                    nic_copy,
                    epf_documents,
                    remarks,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """;

        return jdbcTemplate.update(
                sql,
                employee.getEmployeeCode(),
                employee.getEmployeeType(),
                employee.getTemporaryYNumber(),
                employee.getEpfNumber(),
                employee.getEmployeeStatusId(),
                employee.getJoinedDate(),
                employee.getPermanentAppointmentDate(),
                employee.getDepartment(),
                employee.getDesignationId(),
                employee.getReportingSupervisorCode(),
                employee.getNic(),
                employee.getFullName(),
                employee.getNameWithInitials(),
                employee.getDob(),
                employee.getGender(),
                employee.getMaritalStatus(),
                employee.getNationality(),
                employee.getContactNumber(),
                employee.getAltContactNumber(),
                employee.getEmail(),
                employee.getResidentialAddress(),
                employee.getEmergencyContactPerson(),
                employee.getEmergencyContactNumber(),
                employee.getEmergencyContactRelationship(),
                employee.getBankName(),
                employee.getBankBranch(),
                employee.getBankAccountNumber(),
                employee.getAccountHolderName(),
                employee.getBasicSalary(),
                employee.getProfilePhoto(),
                employee.getNicCopy(),
                employee.getEpfDocuments(),
                employee.getRemarks()
        );
    }


    // =========================================================
    // ADD ASSIGNED PROJECT
    // =========================================================

    public int addAssignedProject(
            String employeeCode,
            Integer projectId) {

        String sql = """
                INSERT INTO assigned_projects (
                    employee_code,
                    project_id
                )
                VALUES (?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                employeeCode,
                projectId
        );
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    public int updateEmployee(
            String employeeCode,
            Employee employee) {

        String sql = """
                UPDATE employee
                SET
                    employee_type = ?,
                    temporary_y_number = ?,
                    epf_number = ?,
                    employeeStatusId = ?,
                    joined_date = ?,
                    permanent_appointment_date = ?,
                    department = ?,
                    designationId = ?,
                    reporting_supervisor_code = ?,
                    nic = ?,
                    full_name = ?,
                    name_with_initials = ?,
                    dob = ?,
                    gender = ?,
                    marital_status = ?,
                    nationality = ?,
                    contact_number = ?,
                    alt_contact_number = ?,
                    email = ?,
                    residential_address = ?,
                    emergency_contact_person = ?,
                    emergency_contact_number = ?,
                    emergency_contact_relationship = ?,
                    bank_name = ?,
                    bank_branch = ?,
                    bank_account_number = ?,
                    account_holder_name = ?,
                    basic_salary = ?,
                    profile_photo = ?,
                    nic_copy = ?,
                    epf_documents = ?,
                    remarks = ?,
                    updated_at = CURRENT_TIMESTAMP

                WHERE employee_code = ?
                """;

        return jdbcTemplate.update(
                sql,
                employee.getEmployeeType(),
                employee.getTemporaryYNumber(),
                employee.getEpfNumber(),
                employee.getEmployeeStatusId(),
                employee.getJoinedDate(),
                employee.getPermanentAppointmentDate(),
                employee.getDepartment(),
                employee.getDesignationId(),
                employee.getReportingSupervisorCode(),
                employee.getNic(),
                employee.getFullName(),
                employee.getNameWithInitials(),
                employee.getDob(),
                employee.getGender(),
                employee.getMaritalStatus(),
                employee.getNationality(),
                employee.getContactNumber(),
                employee.getAltContactNumber(),
                employee.getEmail(),
                employee.getResidentialAddress(),
                employee.getEmergencyContactPerson(),
                employee.getEmergencyContactNumber(),
                employee.getEmergencyContactRelationship(),
                employee.getBankName(),
                employee.getBankBranch(),
                employee.getBankAccountNumber(),
                employee.getAccountHolderName(),
                employee.getBasicSalary(),
                employee.getProfilePhoto(),
                employee.getNicCopy(),
                employee.getEpfDocuments(),
                employee.getRemarks(),
                employeeCode
        );
    }


    // =========================================================
    // DELETE CURRENT PROJECT ASSIGNMENTS
    // =========================================================

    public int deleteAssignedProjects(String employeeCode) {

        String sql = """
                DELETE FROM assigned_projects
                WHERE employee_code = ?
                """;

        return jdbcTemplate.update(
                sql,
                employeeCode
        );
    }
}