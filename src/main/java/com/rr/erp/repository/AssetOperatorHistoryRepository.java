package com.rr.erp.repository;

import com.rr.erp.entity.AssetOperatorHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssetOperatorHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetOperatorHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AssetOperatorHistory> ASSET_OPERATOR_HISTORY_ROW_MAPPER =
            (rs, rowNum) -> {

                AssetOperatorHistory row = new AssetOperatorHistory();

                row.setAssetOperatorHistoryId(rs.getObject("assetOperatorHistoryId", UUID.class));
                row.setAssetCode(rs.getString("assetCode"));
                row.setOperatorEmployeeCode(rs.getString("operatorEmployeeCode"));
                row.setContactNumber(rs.getString("contactNumber"));
                row.setChangedBy(rs.getString("changedBy"));
                row.setChangedDate(rs.getObject("changedDate", LocalDateTime.class));
                row.setRemarks(rs.getString("remarks"));
                row.setIsActive(rs.getObject("isActive", Boolean.class));
                row.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
                row.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));

                return row;
            };

    // POST
    public int createAssetOperatorHistory(AssetOperatorHistory assetOperatorHistory) {

        String sql = """
                INSERT INTO asset_operator_history (
                    asset_operator_history_id,
                    asset_code,
                    operator_employee_code,
                    contact_number,
                    changed_by,
                    changed_date,
                    remarks,
                    created_at,
                    updated_at,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
                """;

        UUID assetOperatorHistoryId = UUID.randomUUID();
        assetOperatorHistory.setAssetOperatorHistoryId(assetOperatorHistoryId);

        return jdbcTemplate.update(
                sql,
                assetOperatorHistory.getAssetOperatorHistoryId(),
                assetOperatorHistory.getAssetCode(),
                assetOperatorHistory.getOperatorEmployeeCode(),
                assetOperatorHistory.getContactNumber(),
                assetOperatorHistory.getChangedBy(),
                assetOperatorHistory.getChangedDate(),
                assetOperatorHistory.getRemarks()
        );
    }

    private static final String SELECT_COLUMNS = """
            SELECT
                asset_operator_history_id AS "assetOperatorHistoryId",
                asset_code AS "assetCode",
                operator_employee_code AS "operatorEmployeeCode",
                contact_number AS "contactNumber",
                changed_by AS "changedBy",
                changed_date AS "changedDate",
                remarks AS "remarks",
                created_at AS "createdAt",
                updated_at AS "updatedAt",
                is_active AS "isActive"
            FROM asset_operator_history
            """;

    // GET BY ASSET CODE
    public List<AssetOperatorHistory> getAssetOperatorHistoryByAssetCode(String assetCode) {

        String sql = SELECT_COLUMNS + """
                WHERE asset_code = ?
                  AND is_active = TRUE
                ORDER BY changed_date DESC, created_at DESC
                """;

        return jdbcTemplate.query(sql, ASSET_OPERATOR_HISTORY_ROW_MAPPER, assetCode);
    }

    // The most recently recorded active operator for an asset — used to auto-fill the operator
    // fields on the Fuel Issue and Service Request forms for that asset.
    public Optional<AssetOperatorHistory> findLatestActiveOperator(String assetCode) {

        String sql = SELECT_COLUMNS + """
                WHERE asset_code = ?
                  AND is_active = TRUE
                ORDER BY changed_date DESC, created_at DESC
                LIMIT 1
                """;

        List<AssetOperatorHistory> results = jdbcTemplate.query(sql, ASSET_OPERATOR_HISTORY_ROW_MAPPER, assetCode);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Every asset whose most recently recorded active operator is this employee — used to filter
    // the asset picker on forms an operator fills in themselves (e.g. Meter Reading) down to just
    // the assets they currently operate.
    public List<String> findAssetCodesForOperator(String operatorEmployeeCode) {

        String sql = """
                SELECT a.asset_code
                FROM asset a
                JOIN LATERAL (
                    SELECT operator_employee_code
                    FROM asset_operator_history h
                    WHERE h.asset_code = a.asset_code
                      AND h.is_active = TRUE
                    ORDER BY h.changed_date DESC, h.created_at DESC
                    LIMIT 1
                ) latest ON TRUE
                WHERE latest.operator_employee_code = ?
                ORDER BY a.asset_code
                """;

        return jdbcTemplate.queryForList(sql, String.class, operatorEmployeeCode);
    }

    // PATCH — only remarks and contact number can be corrected after the fact; the operator,
    // asset, changed-by and changed-date stay fixed at creation. The frontend restricts this to
    // the latest entry for an asset, the same rule as deleting one.
    public int updateAssetOperatorHistoryDetails(UUID assetOperatorHistoryId, String remarks, String contactNumber) {

        String sql = """
                UPDATE asset_operator_history
                SET
                    remarks = ?,
                    contact_number = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE asset_operator_history_id = ?
                """;

        return jdbcTemplate.update(sql, remarks, contactNumber, assetOperatorHistoryId);
    }

    // SOFT DELETE
    public int deleteAssetOperatorHistory(UUID assetOperatorHistoryId) {

        String sql = """
                UPDATE asset_operator_history
                SET
                    is_active = FALSE,
                    updated_at = CURRENT_TIMESTAMP
                WHERE asset_operator_history_id = ?
                """;

        return jdbcTemplate.update(sql, assetOperatorHistoryId);
    }
}
