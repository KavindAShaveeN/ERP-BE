package com.rr.erp.repository;

import com.rr.erp.dto.JobCostSummaryRow;
import com.rr.erp.dto.LaborEntryRow;
import com.rr.erp.dto.ThirdPartyServiceReportRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceReportRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // JOB CARDS WITH ROLLED-UP COST + LABOUR (main and sub job cards)
    // =========================================================
    public List<JobCostSummaryRow> getJobCostSummaries(LocalDate from, LocalDate to) {

        List<Object> params = new ArrayList<>();

        String sql = """
                SELECT
                    jc.job_card_id AS "jobCardId",
                    jc.job_card_code AS "jobCardCode",
                    jc.parent_job_card_id AS "parentJobCardId",
                    pjc.job_card_code AS "parentJobCardCode",
                    jc.asset_code AS "assetCode",
                    jc.make AS "make",
                    jc.type AS "type",
                    jc.job_type AS "jobType",
                    jc.department AS "department",
                    jc.project_code AS "projectCode",
                    jc.meter_reading AS "meterReading",
                    jc.job_status_type_id AS "jobStatusTypeId",
                    jst.job_status_type_name AS "jobStatusTypeName",
                    jc.is_finished AS "isFinished",
                    jc.is_delivered AS "isDelivered",
                    jc.created_date AS "createdDate",
                    jc.updated_at AS "updatedDate",
                    jc.job_checked_date AS "jobCheckedDate",
                    COALESCE((
                        SELECT SUM(COALESCE(ipii.amount, ipii.unit_price * ipii.quantity, 0))
                        FROM intra_project_issue ipi
                        JOIN intra_project_issue_item ipii
                            ON ipii.intra_project_issue_id = ipi.intra_project_issue_id
                        WHERE ipi.job_card_id = jc.job_card_id
                          AND ipi.is_authorized = TRUE
                    ), 0)
                    -- Approved returns of items actually issued to this job card reduce the cost;
                    -- a spare part returned with no source issue line never counted.
                    - COALESCE((
                        SELECT SUM(COALESCE(ipri.amount, ipri.unit_price * ipri.quantity, 0))
                        FROM intra_project_issue_return ipr
                        JOIN intra_project_issue_return_item ipri
                            ON ipri.intra_project_issue_return_id = ipr.intra_project_issue_return_id
                        WHERE ipr.job_card_id = jc.job_card_id
                          AND ipr.is_approved = TRUE
                          AND ipri.intra_project_issue_item_id IS NOT NULL
                    ), 0) AS "sparePartsCost",
                    COALESCE((
                        SELECT SUM(COALESCE(tps.service_charge, 0))
                        FROM three_p_service tps
                        WHERE tps.job_card_id = jc.job_card_id
                    ), 0) AS "thirdPartyCost",
                    COALESCE((
                        SELECT SUM(COALESCE(jce.amount, 0))
                        FROM job_cost_entry jce
                        WHERE jce.job_card_id = jc.job_card_id
                    ), 0) AS "additionalCost",
                    COALESCE((
                        SELECT SUM(COALESCE(jw.hours_worked, 0))
                        FROM job_worker jw
                        WHERE jw.job_card_id = jc.job_card_id
                    ), 0) AS "laborHours",
                    (
                        SELECT COUNT(*)
                        FROM job_worker jw
                        WHERE jw.job_card_id = jc.job_card_id
                    ) AS "workerCount"
                FROM job_card jc
                LEFT JOIN job_card pjc
                    ON pjc.job_card_id = jc.parent_job_card_id
                LEFT JOIN job_status_type jst
                    ON jst.job_status_type_id = jc.job_status_type_id
                WHERE 1 = 1
                """
                + dateRangeClause("jc.created_date", from, to, params)
                + " ORDER BY jc.created_date DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    JobCostSummaryRow row = new JobCostSummaryRow();

                    row.setJobCardId(rs.getObject("jobCardId", UUID.class));
                    row.setJobCardCode(rs.getString("jobCardCode"));
                    row.setParentJobCardId(rs.getObject("parentJobCardId", UUID.class));
                    row.setParentJobCardCode(rs.getString("parentJobCardCode"));
                    row.setAssetCode(rs.getString("assetCode"));
                    row.setMake(rs.getString("make"));
                    row.setType(rs.getString("type"));
                    row.setJobType(rs.getString("jobType"));
                    row.setDepartment(rs.getString("department"));
                    row.setProjectCode(rs.getString("projectCode"));
                    row.setMeterReading(rs.getBigDecimal("meterReading"));
                    row.setJobStatusTypeId(rs.getObject("jobStatusTypeId", Integer.class));
                    row.setJobStatusTypeName(rs.getString("jobStatusTypeName"));
                    row.setIsFinished(rs.getObject("isFinished", Boolean.class));
                    row.setIsDelivered(rs.getObject("isDelivered", Boolean.class));
                    row.setCreatedDate(rs.getObject("createdDate", LocalDateTime.class));
                    row.setUpdatedDate(rs.getObject("updatedDate", LocalDateTime.class));
                    row.setJobCheckedDate(rs.getObject("jobCheckedDate", LocalDateTime.class));

                    BigDecimal spareParts = rs.getBigDecimal("sparePartsCost");
                    BigDecimal thirdParty = rs.getBigDecimal("thirdPartyCost");
                    BigDecimal additional = rs.getBigDecimal("additionalCost");

                    row.setSparePartsCost(spareParts);
                    row.setThirdPartyCost(thirdParty);
                    row.setAdditionalCost(additional);
                    row.setTotalCost(spareParts.add(thirdParty).add(additional));

                    row.setLaborHours(rs.getBigDecimal("laborHours"));
                    row.setWorkerCount(rs.getInt("workerCount"));

                    return row;
                },
                params.toArray()
        );
    }


    // =========================================================
    // EMPLOYEE HOURS PER JOB CARD
    // =========================================================
    public List<LaborEntryRow> getLaborEntries(LocalDate from, LocalDate to) {

        List<Object> params = new ArrayList<>();

        String sql = """
                SELECT
                    jw.employee_code AS "employeeCode",
                    e.full_name AS "fullName",
                    d.designationName AS "designationName",
                    jc.job_card_id AS "jobCardId",
                    jc.job_card_code AS "jobCardCode",
                    jc.asset_code AS "assetCode",
                    jc.job_type AS "jobType",
                    jc.department AS "department",
                    jc.created_date AS "createdDate",
                    COALESCE(jw.hours_worked, 0) AS "hoursWorked"
                FROM job_worker jw
                JOIN job_card jc
                    ON jc.job_card_id = jw.job_card_id
                LEFT JOIN employee e
                    ON e.employee_code = jw.employee_code
                LEFT JOIN designation d
                    ON d.designationId = e.designationId
                WHERE 1 = 1
                """
                + dateRangeClause("jc.created_date", from, to, params)
                + " ORDER BY e.full_name, jc.created_date DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    LaborEntryRow row = new LaborEntryRow();

                    row.setEmployeeCode(rs.getString("employeeCode"));
                    row.setFullName(rs.getString("fullName"));
                    row.setDesignationName(rs.getString("designationName"));
                    row.setJobCardId(rs.getObject("jobCardId", UUID.class));
                    row.setJobCardCode(rs.getString("jobCardCode"));
                    row.setAssetCode(rs.getString("assetCode"));
                    row.setJobType(rs.getString("jobType"));
                    row.setDepartment(rs.getString("department"));
                    row.setCreatedDate(rs.getObject("createdDate", LocalDateTime.class));
                    row.setHoursWorked(rs.getBigDecimal("hoursWorked"));

                    return row;
                },
                params.toArray()
        );
    }


    // =========================================================
    // THIRD PARTY SERVICE ISSUES (items/assets sent out and received back, with cost)
    // =========================================================
    public List<ThirdPartyServiceReportRow> getThirdPartyServiceEntries(LocalDate from, LocalDate to) {

        List<Object> params = new ArrayList<>();

        String sql = """
                SELECT
                    tps.three_p_service_id AS "threePServiceId",
                    tps.job_card_id AS "jobCardId",
                    jc.job_card_code AS "jobCardCode",
                    pjc.job_card_code AS "parentJobCardCode",
                    jc.asset_code AS "vehicleAssetCode",
                    jc.make AS "make",
                    jc.type AS "type",
                    jc.department AS "department",
                    jc.project_code AS "projectCode",
                    tps.item AS "item",
                    tps.serial_number AS "serialNumber",
                    tps.asset_code AS "assetCode",
                    tps.item_code AS "itemCode",
                    tps.quantity AS "quantity",
                    tps.service_provider_name AS "serviceProviderName",
                    tps.issued_date AS "issuedDate",
                    tps.status AS "status",
                    tps.received_date AS "receivedDate",
                    tps.remarks AS "remarks",
                    tps.service_charge AS "serviceCharge"
                FROM three_p_service tps
                JOIN job_card jc
                    ON jc.job_card_id = tps.job_card_id
                LEFT JOIN job_card pjc
                    ON pjc.job_card_id = jc.parent_job_card_id
                WHERE 1 = 1
                """
                + dateRangeClause("tps.issued_date", from, to, params)
                + " ORDER BY tps.issued_date DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    ThirdPartyServiceReportRow row = new ThirdPartyServiceReportRow();

                    row.setThreePServiceId(rs.getObject("threePServiceId", UUID.class));
                    row.setJobCardId(rs.getObject("jobCardId", UUID.class));
                    row.setJobCardCode(rs.getString("jobCardCode"));
                    row.setParentJobCardCode(rs.getString("parentJobCardCode"));
                    row.setVehicleAssetCode(rs.getString("vehicleAssetCode"));
                    row.setMake(rs.getString("make"));
                    row.setType(rs.getString("type"));
                    row.setDepartment(rs.getString("department"));
                    row.setProjectCode(rs.getString("projectCode"));

                    row.setItem(rs.getString("item"));
                    row.setSerialNumber(rs.getString("serialNumber"));
                    row.setAssetCode(rs.getString("assetCode"));
                    row.setItemCode(rs.getString("itemCode"));
                    row.setQuantity(rs.getBigDecimal("quantity"));

                    row.setServiceProviderName(rs.getString("serviceProviderName"));
                    row.setIssuedDate(rs.getObject("issuedDate", LocalDateTime.class));
                    row.setStatus(rs.getString("status"));
                    row.setReceivedDate(rs.getObject("receivedDate", LocalDateTime.class));
                    row.setRemarks(rs.getString("remarks"));
                    row.setServiceCharge(rs.getBigDecimal("serviceCharge"));

                    return row;
                },
                params.toArray()
        );
    }


    /** Inclusive [from, to] day range on a timestamp column; either bound may be omitted. */
    private String dateRangeClause(
            String column,
            LocalDate from,
            LocalDate to,
            List<Object> params) {

        StringBuilder clause = new StringBuilder();

        if (from != null) {
            clause.append(" AND ").append(column).append(" >= ?");
            params.add(from.atStartOfDay());
        }

        if (to != null) {
            clause.append(" AND ").append(column).append(" < ?");
            params.add(to.plusDays(1).atStartOfDay());
        }

        return clause.toString();
    }
}
