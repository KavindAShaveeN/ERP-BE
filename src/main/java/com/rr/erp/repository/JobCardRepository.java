package com.rr.erp.repository;

import com.rr.erp.entity.JobCard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobCardRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // CREATE JOB CARD
    // =========================================================
    public int createJobCard(JobCard jobCard) {

        String sql = """
                INSERT INTO job_card (
                    job_card_id,
                    job_card_code,
                    requesting_project_code,
                    project_code,
                    asset_code,
                    make,
                    type,
                    meter_reading,
                    expected_hours_to_complete,
                    created_date,
                    informed_by,
                    phone_number,
                    department,
                    job_status_type_id,
                    updated_at,
                    job_checked_by,
                    job_checked_date,
                    remarks,
                    is_finished,
                    is_delivered,
                    job_type,
                    parent_job_card_id,
                    service_request_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                jobCard.getJobCardId(),
                jobCard.getJobCardCode(),
                jobCard.getRequestingProjectCode(),
                jobCard.getProjectCode(),
                jobCard.getAssetCode(),
                jobCard.getMake(),
                jobCard.getType(),
                jobCard.getMeterReading(),
                jobCard.getExpectedHoursToComplete(),
                jobCard.getCreatedDate(),
                jobCard.getInformedBy(),
                jobCard.getPhoneNumber(),
                jobCard.getDepartment(),
                jobCard.getJobStatusTypeId(),
                jobCard.getUpdatedDate(),
                jobCard.getJobCheckedBy(),
                jobCard.getJobCheckedDate(),
                jobCard.getRemarks(),
                jobCard.getIsFinished(),
                jobCard.getIsDelivered(),
                jobCard.getJobType(),
                jobCard.getParentJobCardId(),
                jobCard.getServiceRequestId()
        );
    }


    // =========================================================
    // UPDATE JOB CARD
    // =========================================================
    public int updateJobCard(
            UUID jobCardId,
            JobCard jobCard) {

        String sql = """
                UPDATE job_card
                SET
                    job_card_code = ?,
                    requesting_project_code = ?,
                    project_code = ?,
                    asset_code = ?,
                    make = ?,
                    type = ?,
                    meter_reading = ?,
                    expected_hours_to_complete = ?,
                    informed_by = ?,
                    phone_number = ?,
                    department = ?,
                    job_status_type_id = ?,
                    updated_at = ?,
                    job_checked_by = ?,
                    job_checked_date = ?,
                    remarks = ?,
                    is_finished = ?,
                    is_delivered = ?,
                    job_type = ?,
                    parent_job_card_id = ?,
                    service_request_id = ?
                WHERE job_card_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                jobCard.getJobCardCode(),
                jobCard.getRequestingProjectCode(),
                jobCard.getProjectCode(),
                jobCard.getAssetCode(),
                jobCard.getMake(),
                jobCard.getType(),
                jobCard.getMeterReading(),
                jobCard.getExpectedHoursToComplete(),
                jobCard.getInformedBy(),
                jobCard.getPhoneNumber(),
                jobCard.getDepartment(),
                jobCard.getJobStatusTypeId(),
                jobCard.getUpdatedDate(),
                jobCard.getJobCheckedBy(),
                jobCard.getJobCheckedDate(),
                jobCard.getRemarks(),
                jobCard.getIsFinished(),
                jobCard.getIsDelivered(),
                jobCard.getJobType(),
                jobCard.getParentJobCardId(),
                jobCard.getServiceRequestId(),
                jobCardId
        );
    }


    // =========================================================
    // GET ALL JOB CARDS
    // =========================================================
    public List<JobCard> getAllJobCards() {

        String sql = """
                SELECT
                    job_card_id AS "jobCardId",
                    job_card_code AS "jobCardCode",
                    requesting_project_code AS "requestingProjectCode",
                    project_code AS "projectCode",
                    asset_code AS "assetCode",
                    make AS "make",
                    type AS "type",
                    meter_reading AS "meterReading",
                    expected_hours_to_complete AS "expectedHoursToComplete",
                    created_date AS "createdDate",
                    informed_by AS "informedBy",
                    phone_number AS "phoneNumber",
                    department AS "department",
                    job_status_type_id AS "jobStatusTypeId",
                    updated_at AS "updatedDate",
                    job_checked_by AS "jobCheckedBy",
                    job_checked_date AS "jobCheckedDate",
                    remarks AS "remarks",
                    is_finished AS "isFinished",
                    is_delivered AS "isDelivered",
                    cost,
                    job_type AS "jobType",
                    parent_job_card_id AS "parentJobCardId",
                    service_request_id AS "serviceRequestId"
                FROM job_card
                ORDER BY created_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs)
        );
    }


    // =========================================================
    // GET DELIVERED JOB CARDS AWAITING A SERVICE RECEIVE NOTE
    // =========================================================
    // Feeds the requesting project's "Add Service Receive Note" picker — delivered job cards
    // (asset dispatched out of the workshop, see AssetLocationService#dispatchAssetFromJobCard)
    // for this project that don't have a Service Receive Note recorded against them yet.
    public List<JobCard> getPendingServiceReceiveNoteJobCards(String requestingProjectCode) {

        String sql = """
                SELECT
                    jc.job_card_id AS "jobCardId",
                    jc.job_card_code AS "jobCardCode",
                    jc.requesting_project_code AS "requestingProjectCode",
                    jc.project_code AS "projectCode",
                    jc.asset_code AS "assetCode",
                    jc.make AS "make",
                    jc.type AS "type",
                    jc.meter_reading AS "meterReading",
                    jc.expected_hours_to_complete AS "expectedHoursToComplete",
                    jc.created_date AS "createdDate",
                    jc.informed_by AS "informedBy",
                    jc.phone_number AS "phoneNumber",
                    jc.department AS "department",
                    jc.job_status_type_id AS "jobStatusTypeId",
                    jc.updated_at AS "updatedDate",
                    jc.job_checked_by AS "jobCheckedBy",
                    jc.job_checked_date AS "jobCheckedDate",
                    jc.remarks AS "remarks",
                    jc.is_finished AS "isFinished",
                    jc.is_delivered AS "isDelivered",
                    jc.cost,
                    jc.job_type AS "jobType",
                    jc.parent_job_card_id AS "parentJobCardId",
                    jc.service_request_id AS "serviceRequestId"
                FROM job_card jc
                WHERE jc.requesting_project_code = ?
                  AND jc.is_delivered = TRUE
                  AND NOT EXISTS (
                      SELECT 1 FROM service_receive_note srn WHERE srn.job_card_id = jc.job_card_id
                  )
                ORDER BY jc.updated_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs),
                requestingProjectCode
        );
    }


    // =========================================================
    // GET SPECIFIC JOB CARD
    // =========================================================
    public Optional<JobCard> getJobCardById(UUID jobCardId) {

        String sql = """
                SELECT
                    job_card_id AS "jobCardId",
                    job_card_code AS "jobCardCode",
                    requesting_project_code AS "requestingProjectCode",
                    project_code AS "projectCode",
                    asset_code AS "assetCode",
                    make AS "make",
                    type AS "type",
                    meter_reading AS "meterReading",
                    expected_hours_to_complete AS "expectedHoursToComplete",
                    created_date AS "createdDate",
                    informed_by AS "informedBy",
                    phone_number AS "phoneNumber",
                    department AS "department",
                    job_status_type_id AS "jobStatusTypeId",
                    updated_at AS "updatedDate",
                    job_checked_by AS "jobCheckedBy",
                    job_checked_date AS "jobCheckedDate",
                    remarks AS "remarks",
                    is_finished AS "isFinished",
                    is_delivered AS "isDelivered",
                    cost,
                    job_type AS "jobType",
                    parent_job_card_id AS "parentJobCardId",
                    service_request_id AS "serviceRequestId"
                FROM job_card
                WHERE job_card_id = ?
                """;

        List<JobCard> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs),
                jobCardId
        );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }


    // =========================================================
    // MAPPER
    // =========================================================
    private JobCard mapJobCard(ResultSet rs)
            throws SQLException {

        JobCard jobCard = new JobCard();

        jobCard.setJobCardId(
                rs.getObject("jobCardId", UUID.class)
        );

        jobCard.setJobCardCode(
                rs.getString("jobCardCode")
        );

        jobCard.setRequestingProjectCode(
                rs.getString("requestingProjectCode")
        );

        jobCard.setProjectCode(
                rs.getString("projectCode")
        );

        jobCard.setAssetCode(
                rs.getString("assetCode")
        );

        jobCard.setMake(
                rs.getString("make")
        );

        jobCard.setType(
                rs.getString("type")
        );

        jobCard.setMeterReading(
                rs.getBigDecimal("meterReading")
        );

        jobCard.setExpectedHoursToComplete(
                rs.getBigDecimal("expectedHoursToComplete")
        );

        jobCard.setCreatedDate(
                rs.getObject(
                        "createdDate",
                        java.time.LocalDateTime.class
                )
        );

        jobCard.setInformedBy(
                rs.getString("informedBy")
        );

        jobCard.setPhoneNumber(
                rs.getString("phoneNumber")
        );

        jobCard.setDepartment(
                rs.getString("department")
        );

        jobCard.setJobStatusTypeId(
                rs.getObject(
                        "jobStatusTypeId",
                        Integer.class
                )
        );

        jobCard.setUpdatedDate(
                rs.getObject(
                        "updatedDate",
                        java.time.LocalDateTime.class
                )
        );

        jobCard.setJobCheckedBy(
                rs.getString("jobCheckedBy")
        );

        jobCard.setJobCheckedDate(
                rs.getObject(
                        "jobCheckedDate",
                        java.time.LocalDateTime.class
                )
        );

        jobCard.setRemarks(
                rs.getString("remarks")
        );

        jobCard.setIsFinished(
                rs.getObject(
                        "isFinished",
                        Boolean.class
                )
        );

        jobCard.setIsDelivered(
                rs.getObject(
                        "isDelivered",
                        Boolean.class
                )
        );

        jobCard.setCost(
                rs.getBigDecimal("cost")
        );
        jobCard.setJobType(
                rs.getString("jobType")
        );

        jobCard.setParentJobCardId(
                rs.getObject("parentJobCardId", UUID.class)
        );

        jobCard.setServiceRequestId(
                rs.getObject("serviceRequestId", UUID.class)
        );

        return jobCard;
    }


    // =========================================================
    // GET SUB JOB CARDS OF A PARENT
    // =========================================================
    public List<JobCard> getSubJobCards(UUID parentJobCardId) {

        String sql = """
                SELECT
                    job_card_id AS "jobCardId",
                    job_card_code AS "jobCardCode",
                    requesting_project_code AS "requestingProjectCode",
                    project_code AS "projectCode",
                    asset_code AS "assetCode",
                    make AS "make",
                    type AS "type",
                    meter_reading AS "meterReading",
                    expected_hours_to_complete AS "expectedHoursToComplete",
                    created_date AS "createdDate",
                    informed_by AS "informedBy",
                    phone_number AS "phoneNumber",
                    department AS "department",
                    job_status_type_id AS "jobStatusTypeId",
                    updated_at AS "updatedDate",
                    job_checked_by AS "jobCheckedBy",
                    job_checked_date AS "jobCheckedDate",
                    remarks AS "remarks",
                    is_finished AS "isFinished",
                    is_delivered AS "isDelivered",
                    cost,
                    job_type AS "jobType",
                    parent_job_card_id AS "parentJobCardId",
                    service_request_id AS "serviceRequestId"
                FROM job_card
                WHERE parent_job_card_id = ?
                ORDER BY created_date ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs),
                parentJobCardId
        );
    }

    public int upsertJobCardCost(UUID jobCardId, BigDecimal cost) {

        String sql = """
                INSERT INTO job_card (
                    job_card_id,
                    cost
                )
                VALUES (?, ?)
                ON CONFLICT (job_card_id)
                DO UPDATE SET
                    cost = EXCLUDED.cost,
                    updated_at = CURRENT_TIMESTAMP
                """;

        return jdbcTemplate.update(
                sql,
                jobCardId,
                cost
        );
    }

    public List<JobCard> getJobCardsByJobType(String jobType) {

        String sql = """
            SELECT
                job_card_id AS "jobCardId",
                job_card_code AS "jobCardCode",
                requesting_project_code AS "requestingProjectCode",
                project_code AS "projectCode",
                asset_code AS "assetCode",
                make,
                type,
                meter_reading AS "meterReading",
                expected_hours_to_complete AS "expectedHoursToComplete",
                created_date AS "createdDate",
                informed_by AS "informedBy",
                phone_number AS "phoneNumber",
                department AS "department",
                job_status_type_id AS "jobStatusTypeId",
                updated_at AS "updatedDate",
                job_checked_by AS "jobCheckedBy",
                job_checked_date AS "jobCheckedDate",
                remarks,
                is_finished AS "isFinished",
                is_delivered AS "isDelivered",
                cost,
                job_type AS "jobType",
                parent_job_card_id AS "parentJobCardId",
                service_request_id AS "serviceRequestId"
            FROM job_card
            WHERE job_type = ? AND is_finished = TRUE
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs),
                jobType
        );
    }


    // =========================================================
    // GET JOB CARDS BY PROJECT
    // =========================================================
    public List<JobCard> getJobCardsByProject(String projectCode) {

        String sql = """
            SELECT
                job_card_id AS "jobCardId",
                job_card_code AS "jobCardCode",
                requesting_project_code AS "requestingProjectCode",
                project_code AS "projectCode",
                asset_code AS "assetCode",
                make,
                type,
                meter_reading AS "meterReading",
                expected_hours_to_complete AS "expectedHoursToComplete",
                created_date AS "createdDate",
                informed_by AS "informedBy",
                phone_number AS "phoneNumber",
                department AS "department",
                job_status_type_id AS "jobStatusTypeId",
                updated_at AS "updatedDate",
                job_checked_by AS "jobCheckedBy",
                job_checked_date AS "jobCheckedDate",
                remarks,
                is_finished AS "isFinished",
                is_delivered AS "isDelivered",
                cost,
                job_type AS "jobType",
                parent_job_card_id AS "parentJobCardId",
                service_request_id AS "serviceRequestId"
            FROM job_card
            WHERE project_code = ?
            ORDER BY created_date DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCard(rs),
                projectCode
        );
    }
}