package com.rr.erp.repository;

import com.rr.erp.entity.ReceivedService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class ReceivedServiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReceivedServiceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_BASE = """
            SELECT
                rs.received_service_id AS "receivedServiceId",
                rs.grn_id AS "grnId",
                rs.grn_item_id AS "grnItemId",
                g.grn_code AS "grnCode",
                rs.item_code AS "itemCode",
                rs.description AS "description",
                rs.quantity AS "quantity",
                rs.uom_id AS "uomId",
                rs.unit_price AS "unitPrice",
                rs.amount AS "amount",
                rs.project_code AS "projectCode",
                rs.po_code AS "poCode",
                rs.supplier_code AS "supplierCode",
                rs.service_date AS "serviceDate",
                rs.received_by AS "receivedBy"
            FROM received_service rs
            JOIN grn g ON g.grn_id = rs.grn_id
            """;

    public UUID insert(ReceivedService receivedService) {

        UUID id = receivedService.getReceivedServiceId() != null
                ? receivedService.getReceivedServiceId()
                : UUID.randomUUID();
        receivedService.setReceivedServiceId(id);

        String sql = """
                INSERT INTO received_service (
                    received_service_id,
                    grn_id,
                    grn_item_id,
                    item_code,
                    description,
                    quantity,
                    uom_id,
                    unit_price,
                    amount,
                    project_code,
                    po_code,
                    supplier_code,
                    service_date,
                    received_by
                )
                VALUES (
                    :receivedServiceId,
                    :grnId,
                    :grnItemId,
                    :itemCode,
                    :description,
                    :quantity,
                    :uomId,
                    :unitPrice,
                    :amount,
                    :projectCode,
                    :poCode,
                    :supplierCode,
                    :serviceDate,
                    :receivedBy
                )
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("receivedServiceId", id)
                .addValue("grnId", receivedService.getGrnId())
                .addValue("grnItemId", receivedService.getGrnItemId())
                .addValue("itemCode", receivedService.getItemCode())
                .addValue("description", receivedService.getDescription())
                .addValue("quantity", receivedService.getQuantity())
                .addValue("uomId", receivedService.getUomId())
                .addValue("unitPrice", receivedService.getUnitPrice())
                .addValue("amount", receivedService.getAmount())
                .addValue("projectCode", receivedService.getProjectCode())
                .addValue("poCode", receivedService.getPoCode())
                .addValue("supplierCode", receivedService.getSupplierCode())
                .addValue("serviceDate", receivedService.getServiceDate())
                .addValue("receivedBy", receivedService.getReceivedBy());

        jdbcTemplate.update(sql, parameters);

        return id;
    }

    public List<ReceivedService> findByProjectCode(String projectCode) {

        String sql = SELECT_BASE + """
                WHERE rs.project_code = :projectCode
                ORDER BY rs.service_date DESC, g.grn_code DESC
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("projectCode", projectCode);

        return jdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(ReceivedService.class));
    }

    public List<ReceivedService> findAll() {

        String sql = SELECT_BASE + """
                ORDER BY rs.service_date DESC, g.grn_code DESC
                """;

        return jdbcTemplate.query(sql, Collections.emptyMap(), new BeanPropertyRowMapper<>(ReceivedService.class));
    }
}
