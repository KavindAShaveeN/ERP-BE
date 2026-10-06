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
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
                service.getPoCode()
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

    // GET BY JOB CARD ID
    public List<ThreePService> getThreePServicesByJobCardId(UUID jobCardId) {

        String sql = """
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
                    tps.received_date AS "receivedDate",
                    tps.remarks AS "remarks",
                    tps.service_charge AS "serviceCharge",
                    tps.po_code AS "poCode",
                    tps.created_at AS "createdAt",
                    tps.updated_at AS "updatedAt"
                FROM three_p_service tps
                WHERE tps.job_card_id = ?
                ORDER BY tps.created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                this::mapThreePService,
                jobCardId
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