package com.rr.erp.repository;

import com.rr.erp.dto.OperatorAssetResponse;
import com.rr.erp.entity.Asset;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AssetRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            asset_code_id AS "assetCodeId",
            asset_class AS "assetClass",
            description AS "description",
            serial_number AS "serialNumber",
            project_or_department AS "projectOrDepartment",
            ownership_type AS "ownershipType",
            status AS "status",
            condition AS "condition",
            remarks AS "remarks",
            document_name AS "documentName",
            created_at AS "createdAt",
            updated_at AS "updatedAt"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Asset asset) {

        String sql = """
                INSERT INTO asset (
                    asset_code,
                    asset_code_id,
                    asset_class,
                    description,
                    serial_number,
                    project_or_department,
                    ownership_type,
                    status,
                    condition,
                    remarks,
                    document_name,
                    created_at,
                    updated_at
                )
                VALUES (
                    :assetCode,
                    :assetCodeId,
                    :assetClass,
                    :description,
                    :serialNumber,
                    :projectOrDepartment,
                    :ownershipType,
                    :status,
                    :condition,
                    :remarks,
                    :documentName,
                    :createdAt,
                    :updatedAt
                )
                """;

        jdbcTemplate.update(sql, toParameters(asset));
    }

    public int update(Asset asset) {

        String sql = """
                UPDATE asset
                SET
                    asset_code_id = :assetCodeId,
                    description = :description,
                    serial_number = :serialNumber,
                    project_or_department = :projectOrDepartment,
                    ownership_type = :ownershipType,
                    status = :status,
                    condition = :condition,
                    remarks = :remarks,
                    document_name = :documentName,
                    updated_at = :updatedAt
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(asset));
    }

    /** Updates only the status column — used to sync an asset's status from job card
     * events (e.g. a breakdown job opening sets it Out of Service) without disturbing
     * any of its other fields. */
    public int updateStatus(String assetCode, String status) {

        String sql = """
                UPDATE asset
                SET
                    status = :status,
                    updated_at = :updatedAt
                WHERE asset_code = :assetCode
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode)
                .addValue("status", status)
                .addValue("updatedAt", java.time.LocalDateTime.now());

        return jdbcTemplate.update(sql, parameters);
    }

    public Optional<Asset> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM asset WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<Asset> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(Asset.class)
        );

        return results.stream().findFirst();
    }

    public List<Asset> findByAssetClass(String assetClass) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM asset WHERE asset_class = :assetClass ORDER BY asset_code";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetClass", assetClass);

        return jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(Asset.class)
        );
    }

    /** Every asset whose most recently recorded active operator is this employee — same rule as
     * AssetOperatorHistoryRepository.findAssetCodesForOperator, plus each asset's code id and class. */
    public List<OperatorAssetResponse> findByCurrentOperator(String operatorEmployeeCode) {

        String sql = """
                SELECT a.asset_code, a.asset_code_id, a.asset_class
                FROM asset a
                JOIN LATERAL (
                    SELECT h.operator_employee_code
                    FROM asset_operator_history h
                    WHERE h.asset_code = a.asset_code
                      AND h.is_active = TRUE
                    ORDER BY h.changed_date DESC, h.created_at DESC
                    LIMIT 1
                ) latest ON TRUE
                WHERE latest.operator_employee_code = :operatorEmployeeCode
                ORDER BY a.asset_code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("operatorEmployeeCode", operatorEmployeeCode);

        return jdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> new OperatorAssetResponse(
                        rs.getString("asset_code"),
                        rs.getObject("asset_code_id", Long.class),
                        rs.getString("asset_class")
                )
        );
    }

    /** Every registered asset code across all classes, in one query. */
    public List<String> findAllAssetCodes() {
        return jdbcTemplate.queryForList(
                "SELECT asset_code FROM asset ORDER BY asset_code",
                new MapSqlParameterSource(),
                String.class
        );
    }

    /** Asset codes matching the same filters as {@link #findPage}, without paging. */
    public List<String> findMatchingAssetCodes(String search, String assetClass, String status) {
        return jdbcTemplate.queryForList(
                "SELECT asset_code FROM asset " + PAGE_FILTER + " ORDER BY asset_code",
                pageFilterParameters(search, assetClass, status),
                String.class
        );
    }

    private static final String PAGE_FILTER = """
            WHERE (CAST(:assetClass AS VARCHAR) IS NULL OR asset_class = CAST(:assetClass AS VARCHAR))
              AND (CAST(:status AS VARCHAR) IS NULL OR status = CAST(:status AS VARCHAR))
              AND (CAST(:search AS VARCHAR) IS NULL
                   OR asset_code ILIKE CAST(:search AS VARCHAR) ESCAPE '\\'
                   OR description ILIKE CAST(:search AS VARCHAR) ESCAPE '\\'
                   OR serial_number ILIKE CAST(:search AS VARCHAR) ESCAPE '\\')
            """;

    private MapSqlParameterSource pageFilterParameters(String search, String assetClass, String status) {
        String pattern = null;
        if (search != null && !search.isBlank()) {
            String escaped = search.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            pattern = "%" + escaped + "%";
        }
        return new MapSqlParameterSource()
                .addValue("search", pattern)
                .addValue("assetClass", assetClass == null || assetClass.isBlank() ? null : assetClass)
                .addValue("status", status == null || status.isBlank() ? null : status);
    }

    /** Common fields only (no class-specific detail), filtered and paged in the database. */
    public List<Asset> findPage(String search, String assetClass, String status, int limit, int offset) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM asset " + PAGE_FILTER
                + " ORDER BY asset_code LIMIT :limit OFFSET :offset";

        MapSqlParameterSource parameters = pageFilterParameters(search, assetClass, status)
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Asset.class));
    }

    public long countPage(String search, String assetClass, String status) {

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asset " + PAGE_FILTER,
                pageFilterParameters(search, assetClass, status),
                Long.class
        );

        return count == null ? 0 : count;
    }

    public boolean existsByAssetCode(String assetCode) {

        String sql = "SELECT COUNT(*) FROM asset WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    private MapSqlParameterSource toParameters(Asset asset) {
        return new MapSqlParameterSource()
                .addValue("assetCode", asset.getAssetCode())
                .addValue("assetCodeId", asset.getAssetCodeId())
                .addValue("assetClass", asset.getAssetClass())
                .addValue("description", asset.getDescription())
                .addValue("serialNumber", asset.getSerialNumber())
                .addValue("projectOrDepartment", asset.getProjectOrDepartment())
                .addValue("ownershipType", asset.getOwnershipType())
                .addValue("status", asset.getStatus())
                .addValue("condition", asset.getCondition())
                .addValue("remarks", asset.getRemarks())
                .addValue("documentName", asset.getDocumentName())
                .addValue("createdAt", asset.getCreatedAt())
                .addValue("updatedAt", asset.getUpdatedAt());
    }
}
