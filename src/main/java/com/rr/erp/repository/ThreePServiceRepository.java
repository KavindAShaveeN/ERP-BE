package com.rr.erp.repository;

import com.rr.erp.entity.ThreePService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class ThreePServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThreePServiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // POST
    public ThreePService createThreePService(ThreePService service) {

        UUID threePServiceId = UUID.randomUUID();

        String sql = """
                INSERT INTO three_p_service (
                    three_p_service_id,
                    job_card_id,
                    item,
                    serial_number,
                    asset_code,
                    item_code,
                    quantity,
                    issued_date,
                    status,
                    service_provider_name,
                    received_date,
                    remarks,
                    service_charge,
                    po_code,
                    service_provider_code,
                    request_group_id,
                    issue_mode,
                    description,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        jdbcTemplate.update(
                sql,
                threePServiceId,
                service.getJobCardId(),
                service.getItem(),
                service.getSerialNumber(),
                service.getAssetCode(),
                service.getItemCode(),
                service.getQuantity(),
                service.getIssuedDate(),
                service.getStatus(),
                service.getServiceProviderName(),
                service.getReceivedDate(),
                service.getRemarks(),
                service.getServiceCharge(),
                service.getPoCode(),
                service.getServiceProviderCode(),
                service.getRequestGroupId(),
                service.getIssueMode() != null ? service.getIssueMode() : "DIRECT",
                service.getDescription()
        );

        service.setThreePServiceId(threePServiceId);

        return service;
    }

    // PUT
    public int updateThreePService(
            UUID threePServiceId,
            ThreePService service
    ) {

        String sql = """
                UPDATE three_p_service
                SET
                    job_card_id = ?,
                    item = ?,
                    serial_number = ?,
                    asset_code = ?,
                    item_code = ?,
                    quantity = ?,
                    issued_date = ?,
                    status = ?,
                    service_provider_name = ?,
                    received_date = ?,
                    remarks = ?,
                    service_charge = ?,
                    po_code = ?,
                    service_provider_code = ?,
                    issue_mode = COALESCE(?, issue_mode),
                    description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE three_p_service_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                service.getJobCardId(),
                service.getItem(),
                service.getSerialNumber(),
                service.getAssetCode(),
                service.getItemCode(),
                service.getQuantity(),
                service.getIssuedDate(),
                service.getStatus(),
                service.getServiceProviderName(),
                service.getReceivedDate(),
                service.getRemarks(),
                service.getServiceCharge(),
                service.getPoCode(),
                service.getServiceProviderCode(),
                service.getIssueMode(),
                service.getDescription(),
                threePServiceId
        );
    }

    // DELETE
    public int deleteThreePService(UUID threePServiceId) {

        String sql = """
                DELETE FROM three_p_service
                WHERE three_p_service_id = ?
                """;

        return jdbcTemplate.update(sql, threePServiceId);
    }

    private static final String SELECT_WITH_JOB_CARD = """
            SELECT
                tps.three_p_service_id AS "threePServiceId",
                tps.job_card_id AS "jobCardId",
                tps.item AS "item",
                tps.serial_number AS "serialNumber",
                tps.asset_code AS "assetCode",
                tps.item_code AS "itemCode",
                tps.quantity AS "quantity",
                tps.issued_date AS "issuedDate",
                tps.status AS "status",
                tps.service_provider_name AS "serviceProviderName",
                tps.service_provider_code AS "serviceProviderCode",
                tps.request_group_id AS "requestGroupId",
                tps.issue_mode AS "issueMode",
                tps.description AS "description",
                tps.received_date AS "receivedDate",
                tps.remarks AS "remarks",
                tps.service_charge AS "serviceCharge",
                tps.po_code AS "poCode",
                tps.created_at AS "createdAt",
                tps.updated_at AS "updatedAt",
                jc.job_card_code AS "jobCardCode",
                jc.project_code AS "projectCode"
            FROM three_p_service tps
            JOIN job_card jc ON jc.job_card_id = tps.job_card_id
            """;

    // GET BY JOB CARD ID
    public List<ThreePService> getThreePServicesByJobCardId(UUID jobCardId) {

        return jdbcTemplate.query(
                SELECT_WITH_JOB_CARD + " WHERE tps.job_card_id = ? ORDER BY tps.created_at DESC",
                this::mapThreePService,
                jobCardId
        );
    }

    // GET every PURCHASING-mode row, whatever its status
    public List<ThreePService> getPurchasingRequests() {

        return jdbcTemplate.query(
                SELECT_WITH_JOB_CARD + " WHERE tps.issue_mode = 'PURCHASING' ORDER BY tps.created_at DESC",
                this::mapThreePService
        );
    }

    // GET PURCHASING-mode rows with one exact status
    public List<ThreePService> getThreePServicesByStatus(String status) {

        return jdbcTemplate.query(
                SELECT_WITH_JOB_CARD
                        + " WHERE tps.issue_mode = 'PURCHASING' AND tps.status = ? ORDER BY tps.created_at DESC",
                this::mapThreePService,
                status
        );
    }

    /**
     * Links rows to a PO. Requested becomes PO Created; already-issued rows keep their status.
     * Provider code/name are filled from the PO supplier only when empty. Rows that already
     * have a PO, or are Completed/Cancelled, are left alone.
     */
    public int markPoCreated(List<UUID> ids, String poCode, String supplierCode) {

        String sql = """
                UPDATE three_p_service
                SET
                    po_code = ?,
                    status = CASE WHEN status = 'Requested' THEN 'PO Created' ELSE status END,
                    service_provider_code = CASE
                        WHEN service_provider_code IS NULL OR service_provider_code = '' THEN ?
                        ELSE service_provider_code END,
                    service_provider_name = CASE
                        WHEN service_provider_name IS NULL OR service_provider_name = ''
                            THEN (SELECT s.suppliername FROM supplier s WHERE s.suppliercode = ?)
                        ELSE service_provider_name END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE three_p_service_id = ?
                  AND (po_code IS NULL OR po_code = '')
                  AND COALESCE(status, '') NOT IN ('Completed', 'Cancelled')
                """;

        int updated = 0;
        for (UUID id : ids) {
            updated += jdbcTemplate.update(sql, poCode, supplierCode, supplierCode, id);
        }
        return updated;
    }

    // The PO line amount (value before tax) becomes the row's service charge.
    public int setServiceCharge(UUID threePServiceId, String poCode, java.math.BigDecimal amount) {

        return jdbcTemplate.update(
                """
                UPDATE three_p_service
                SET service_charge = ?, updated_at = CURRENT_TIMESTAMP
                WHERE three_p_service_id = ? AND po_code = ?
                """,
                amount, threePServiceId, poCode
        );
    }

    // GRN received: every open row on the PO is done.
    public int completeByPoCode(String poCode, java.time.LocalDate grnDate) {

        return jdbcTemplate.update(
                """
                UPDATE three_p_service
                SET status = 'Completed', received_date = ?, updated_at = CURRENT_TIMESTAMP
                WHERE po_code = ? AND status IN ('PO Created', 'Pending', 'In Progress')
                """,
                grnDate != null ? grnDate.atStartOfDay() : java.time.LocalDateTime.now(),
                poCode
        );
    }

    // Completed with a PO, updated in the last 7 days - drives the "ready" notification.
    public List<ThreePService> getRecentlyReceivedByProject(String projectCode) {

        return jdbcTemplate.query(
                SELECT_WITH_JOB_CARD
                        + " WHERE jc.project_code = ? AND tps.status = 'Completed'"
                        + " AND tps.po_code IS NOT NULL AND tps.po_code <> ''"
                        + " AND tps.updated_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'"
                        + " ORDER BY tps.updated_at DESC",
                this::mapThreePService,
                projectCode
        );
    }

    private ThreePService mapThreePService(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        ThreePService service = new ThreePService();

        service.setThreePServiceId(
                rs.getObject("threePServiceId", UUID.class)
        );

        service.setJobCardId(
                rs.getObject("jobCardId", UUID.class)
        );

        service.setItem(
                rs.getString("item")
        );

        service.setSerialNumber(
                rs.getString("serialNumber")
        );

        service.setAssetCode(
                rs.getString("assetCode")
        );

        service.setItemCode(
                rs.getString("itemCode")
        );

        service.setQuantity(
                rs.getBigDecimal("quantity")
        );

        service.setIssuedDate(
                rs.getObject("issuedDate", java.time.LocalDateTime.class)
        );

        service.setStatus(
                rs.getString("status")
        );

        service.setServiceProviderName(
                rs.getString("serviceProviderName")
        );

        service.setServiceProviderCode(rs.getString("serviceProviderCode"));
        service.setRequestGroupId(rs.getObject("requestGroupId", UUID.class));
        service.setIssueMode(rs.getString("issueMode"));
        service.setDescription(rs.getString("description"));
        service.setJobCardCode(rs.getString("jobCardCode"));
        service.setProjectCode(rs.getString("projectCode"));

        service.setReceivedDate(
                rs.getObject("receivedDate", java.time.LocalDateTime.class)
        );

        service.setRemarks(
                rs.getString("remarks")
        );

        service.setServiceCharge(
                rs.getBigDecimal("serviceCharge")
        );

        service.setPoCode(
                rs.getString("poCode")
        );

        service.setCreatedAt(
                rs.getObject("createdAt", java.time.LocalDateTime.class)
        );

        service.setUpdatedAt(
                rs.getObject("updatedAt", java.time.LocalDateTime.class)
        );

        return service;
    }
}