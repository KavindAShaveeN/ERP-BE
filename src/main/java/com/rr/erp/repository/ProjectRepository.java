package com.rr.erp.repository;

import com.rr.erp.dto.ProjectResponseDTO;
import com.rr.erp.entity.Project;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public class ProjectRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProjectRepository(NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer insertProject (Project project)
    {
        String sql = """
                INSERT INTO project
                (
                  project_code,
                  projectName,
                  project_type_id,
                  plant_type,
                  startDate,
                  endDate,
                  value,
                  finishValue,
                  contractNumber,
                  contactNumber,
                  client,
                  area,
                  district,
                  bomLink,
                  boqLink,
                  projectStatusId,
                  description,
                  actual_end_date,
                  pm_code,
                  site_eng_code,
                  asst_eng_code,
                  admin_code,
                  sk_code,
                  qs_code,
                  client_contact_person,
                  client_contact_number,
                  client_email,
                  remarks,
                  created_at,
                  updated_at
                )
                VALUES
                (
                  :projectCode,
                  :projectName,
                  :projectTypeId,
                  :plantType,
                  :startDate,
                  :endDate,
                  :value,
                  :finishValue,
                  :contractNumber,
                  :contactNumber,
                  :client,
                  :area,
                  :district,
                  :bomLink,
                  :boqLink,
                  :projectStatusId,
                  :description,
                  :actualEndDate,
                  :pmCode,
                  :siteEngCode,
                  :asstEngCode,
                  :adminCode,
                  :skCode,
                  :qsCode,
                  :clientContactPerson,
                  :clientContactNumber,
                  :clientEmail,
                  :remarks,
                  :createdAt,
                  :updatedAt
                )
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, toParameters(project), keyHolder, new String[] { "projectid" });

        return keyHolder.getKey().intValue();
    }

    public List<ProjectResponseDTO> getAllProjects() {

        String sql = """
                SELECT
                    p.projectId AS "projectId",
                    p.project_Code AS "projectCode",
                    p.projectName AS "projectName",
                    p.project_type_id AS "projectTypeId",
                    pt.project_type_name AS "projectTypeName",
                    p.plant_type AS "plantType",
                    p.startDate AS "startDate",
                    p.endDate AS "endDate",
                    p.value AS "value",
                    p.finishValue AS "finishValue",
                    p.contractNumber AS "contractNumber",
                    p.contactNumber AS "contactNumber",
                    p.client AS "client",
                    p.area AS "area",
                    p.district AS "district",
                    p.bomLink AS "bomLink",
                    p.boqLink AS "boqLink",
                    p.projectStatusId AS "projectStatusId",
                    ps.projectStatusName AS "projectStatusName",
                    p.description AS "description",
                    p.actual_end_date AS "actualEndDate",
                    p.pm_code AS "pmCode",
                    pm.full_name AS "pmName",
                    p.site_eng_code AS "siteEngCode",
                    site_eng.full_name AS "siteEngName",
                    p.asst_eng_code AS "asstEngCode",
                    asst_eng.full_name AS "asstEngName",
                    p.admin_code AS "adminCode",
                    admin_emp.full_name AS "adminName",
                    p.sk_code AS "skCode",
                    sk.full_name AS "skName",
                    p.qs_code AS "qsCode",
                    qs.full_name AS "qsName",
                    p.client_contact_person AS "clientContactPerson",
                    p.client_contact_number AS "clientContactNumber",
                    p.client_email AS "clientEmail",
                    p.remarks AS "remarks",
                    p.created_at AS "createdAt",
                    p.updated_at AS "updatedAt"
                FROM project p
                LEFT JOIN employee pm
                    ON p.pm_code = pm.employee_code
                LEFT JOIN employee site_eng
                    ON p.site_eng_code = site_eng.employee_code
                LEFT JOIN employee asst_eng
                    ON p.asst_eng_code = asst_eng.employee_code
                LEFT JOIN employee admin_emp
                    ON p.admin_code = admin_emp.employee_code
                LEFT JOIN employee sk
                    ON p.sk_code = sk.employee_code
                LEFT JOIN employee qs
                    ON p.qs_code = qs.employee_code
                LEFT JOIN projectStatus ps
                    ON p.projectStatusId = ps.projectStatusId
                LEFT JOIN project_type pt
                    ON p.project_type_id = pt.project_type_id
                ORDER BY p.projectId DESC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of(),
                new BeanPropertyRowMapper<>(ProjectResponseDTO.class)
        );
    }

    public int updateProject(
            Integer projectId,
            Project projectRequest
    ) {

        String sql = """
            UPDATE project
            SET
                project_Code = :projectCode,
                projectName = :projectName,
                project_type_id = :projectTypeId,
                plant_type = :plantType,
                startDate = :startDate,
                endDate = :endDate,
                value = :value,
                finishValue = :finishValue,
                contractNumber = :contractNumber,
                contactNumber = :contactNumber,
                client = :client,
                area = :area,
                district = :district,
                bomLink = :bomLink,
                boqLink = :boqLink,
                projectStatusId = :projectStatusId,
                description = :description,
                actual_end_date = :actualEndDate,
                pm_code = :pmCode,
                site_eng_code = :siteEngCode,
                asst_eng_code = :asstEngCode,
                admin_code = :adminCode,
                sk_code = :skCode,
                qs_code = :qsCode,
                client_contact_person = :clientContactPerson,
                client_contact_number = :clientContactNumber,
                client_email = :clientEmail,
                remarks = :remarks,
                updated_at = :updatedAt
            WHERE projectId = :projectId
            """;

        MapSqlParameterSource parameters = toParameters(projectRequest);
        parameters.addValue("projectId", projectId);

        return jdbcTemplate.update(sql, parameters);
    }

    private MapSqlParameterSource toParameters(Project project) {
        return new MapSqlParameterSource()
                .addValue("projectCode", project.getProjectCode())
                .addValue("projectName", project.getProjectName())
                .addValue("projectTypeId", project.getProjectTypeId())
                .addValue("plantType", project.getPlantType())
                .addValue("startDate", project.getStartDate())
                .addValue("endDate", project.getEndDate())
                .addValue("value", project.getValue())
                .addValue("finishValue", project.getFinishValue())
                .addValue("contractNumber", project.getContractNumber())
                .addValue("contactNumber", project.getContactNumber())
                .addValue("client", project.getClient())
                .addValue("area", project.getArea())
                .addValue("district", project.getDistrict())
                .addValue("bomLink", project.getBomLink())
                .addValue("boqLink", project.getBoqLink())
                .addValue("projectStatusId", project.getProjectStatusId())
                .addValue("description", project.getDescription())
                .addValue("actualEndDate", project.getActualEndDate())
                .addValue("pmCode", project.getPmCode())
                .addValue("siteEngCode", project.getSiteEngCode())
                .addValue("asstEngCode", project.getAsstEngCode())
                .addValue("adminCode", project.getAdminCode())
                .addValue("skCode", project.getSkCode())
                .addValue("qsCode", project.getQsCode())
                .addValue("clientContactPerson", project.getClientContactPerson())
                .addValue("clientContactNumber", project.getClientContactNumber())
                .addValue("clientEmail", project.getClientEmail())
                .addValue("remarks", project.getRemarks())
                .addValue("createdAt", project.getCreatedAt())
                .addValue("updatedAt", project.getUpdatedAt());
    }
}
