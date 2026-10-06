package com.rr.erp.repository;

import com.rr.erp.entity.StockAdjustment;
import com.rr.erp.entity.StockAdjustmentItem;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockAdjustmentRepository {

    private final JdbcTemplate jdbcTemplate;


    public int createStockAdjustment(
            StockAdjustment adjustment
    ) {

        String sql = """
                INSERT INTO stock_adjustment (
                    stock_adjustment_id,
                    stock_adjustment_code,
                    project_code,
                    adjustment_date,
                    reason,
                    approved_by,
                    approved_date,
                    is_approved,
                    remarks
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                adjustment.getStockAdjustmentId(),
                adjustment.getStockAdjustmentCode(),
                adjustment.getProjectCode(),
                adjustment.getAdjustmentDate(),
                adjustment.getReason(),
                adjustment.getApprovedBy(),
                adjustment.getApprovedDate(),
                adjustment.getIsApproved(),
                adjustment.getRemarks()
        );
    }


    public int createStockAdjustmentItem(
            UUID stockAdjustmentId,
            StockAdjustmentItem item
    ) {

        String sql = """
                INSERT INTO stock_adjustment_item (
                    stock_adjustment_item_id,
                    stock_adjustment_id,
                    item_code,
                    description,
                    uom_id,
                    adjustment_quantity,
                    unit_price,
                    adjustment_value,
                    remarks,
                    length_m,
                    width_m,
                    asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                item.getStockAdjustmentItemId(),
                stockAdjustmentId,
                item.getItemCode(),
                item.getDescription(),
                item.getUomId(),
                item.getAdjustmentQuantity(),
                item.getUnitPrice(),
                item.getAdjustmentValue(),
                item.getRemarks(),
                item.getLengthM(),
                item.getWidthM(),
                item.getAssetCode()
        );
    }

    /** One adjustment by id, no items — used to check its current is_approved state before an update is applied. */
    public Optional<StockAdjustment> findById(
            UUID stockAdjustmentId
    ) {

        String sql = """
                SELECT *
                FROM stock_adjustment
                WHERE stock_adjustment_id = ?
                """;

        try {
            StockAdjustment adjustment = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> {

                        StockAdjustment result = new StockAdjustment();

                        result.setStockAdjustmentId(
                                rs.getObject("stock_adjustment_id", UUID.class)
                        );
                        result.setStockAdjustmentCode(
                                rs.getString("stock_adjustment_code")
                        );
                        result.setProjectCode(
                                rs.getString("project_code")
                        );
                        result.setAdjustmentDate(
                                rs.getObject("adjustment_date", LocalDateTime.class)
                        );
                        result.setReason(
                                rs.getString("reason")
                        );
                        result.setApprovedBy(
                                rs.getString("approved_by")
                        );
                        result.setApprovedDate(
                                rs.getObject("approved_date", LocalDateTime.class)
                        );
                        result.setIsApproved(
                                rs.getBoolean("is_approved")
                        );
                        result.setRemarks(
                                rs.getString("remarks")
                        );

                        return result;
                    },
                    stockAdjustmentId
            );

            return Optional.ofNullable(adjustment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public List<StockAdjustment> getCreatedAdjustments(
            String projectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT *
                FROM stock_adjustment
                WHERE project_code = ?
                ORDER BY adjustment_date DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    StockAdjustment adjustment =
                            new StockAdjustment();

                    adjustment.setStockAdjustmentId(
                            rs.getObject(
                                    "stock_adjustment_id",
                                    UUID.class
                            )
                    );

                    adjustment.setStockAdjustmentCode(
                            rs.getString(
                                    "stock_adjustment_code"
                            )
                    );

                    adjustment.setProjectCode(
                            rs.getString("project_code")
                    );

                    adjustment.setAdjustmentDate(
                            rs.getObject(
                                    "adjustment_date",
                                    LocalDateTime.class
                            )
                    );

                    adjustment.setReason(
                            rs.getString("reason")
                    );

                    adjustment.setApprovedBy(
                            rs.getString("approved_by")
                    );

                    adjustment.setApprovedDate(
                            rs.getObject(
                                    "approved_date",
                                    LocalDateTime.class
                            )
                    );

                    adjustment.setIsApproved(
                            rs.getBoolean("is_approved")
                    );

                    adjustment.setRemarks(
                            rs.getString("remarks")
                    );

                    return adjustment;
                },
                projectCode,
                size,
                page * size
        );
    }

    public long countCreatedAdjustments(String projectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM stock_adjustment
                WHERE project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, projectCode);

        return count == null ? 0 : count;
    }


    // Every stock adjustment across every project — used by HQ-wide report views.
    public List<StockAdjustment> getAllAdjustments(int page, int size) {

        String sql = """
                SELECT *
                FROM stock_adjustment
                ORDER BY adjustment_date DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    StockAdjustment adjustment =
                            new StockAdjustment();

                    adjustment.setStockAdjustmentId(
                            rs.getObject(
                                    "stock_adjustment_id",
                                    UUID.class
                            )
                    );

                    adjustment.setStockAdjustmentCode(
                            rs.getString(
                                    "stock_adjustment_code"
                            )
                    );

                    adjustment.setProjectCode(
                            rs.getString("project_code")
                    );

                    adjustment.setAdjustmentDate(
                            rs.getObject(
                                    "adjustment_date",
                                    LocalDateTime.class
                            )
                    );

                    adjustment.setReason(
                            rs.getString("reason")
                    );

                    adjustment.setApprovedBy(
                            rs.getString("approved_by")
                    );

                    adjustment.setApprovedDate(
                            rs.getObject(
                                    "approved_date",
                                    LocalDateTime.class
                            )
                    );

                    adjustment.setIsApproved(
                            rs.getBoolean("is_approved")
                    );

                    adjustment.setRemarks(
                            rs.getString("remarks")
                    );

                    return adjustment;
                },
                size,
                page * size
        );
    }

    public long countAllAdjustments() {

        String sql = """
                SELECT COUNT(*)
                FROM stock_adjustment
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }


    public List<StockAdjustmentItem> getItems(
            UUID stockAdjustmentId
    ) {

        String sql = """
                SELECT *
                FROM stock_adjustment_item
                WHERE stock_adjustment_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    StockAdjustmentItem item =
                            new StockAdjustmentItem();

                    item.setStockAdjustmentItemId(
                            rs.getObject(
                                    "stock_adjustment_item_id",
                                    UUID.class
                            )
                    );

                    item.setItemCode(
                            rs.getString("item_code")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setUomId(
                            rs.getObject(
                                    "uom_id",
                                    Integer.class
                            )
                    );

                    item.setAdjustmentQuantity(
                            rs.getObject(
                                    "adjustment_quantity",
                                    java.math.BigDecimal.class
                            )
                    );

                    item.setUnitPrice(
                            rs.getBigDecimal("unit_price")
                    );

                    item.setAdjustmentValue(
                            rs.getBigDecimal(
                                    "adjustment_value"
                            )
                    );

                    item.setRemarks(
                            rs.getString("remarks")
                    );

                    item.setLengthM(
                            rs.getBigDecimal("length_m")
                    );

                    item.setWidthM(
                            rs.getBigDecimal("width_m")
                    );

                    item.setAssetCode(
                            rs.getString("asset_code")
                    );

                    return item;
                },
                stockAdjustmentId
        );
    }

    public int updateStockAdjustment(
            UUID stockAdjustmentId,
            StockAdjustment adjustment
    ) {

        String sql = """
                UPDATE stock_adjustment
                SET
                    stock_adjustment_code = ?,
                    project_code = ?,
                    adjustment_date = ?,
                    reason = ?,
                    approved_by = ?,
                    approved_date = ?,
                    is_approved = ?,
                    remarks = ?
                WHERE stock_adjustment_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                adjustment.getStockAdjustmentCode(),
                adjustment.getProjectCode(),
                adjustment.getAdjustmentDate(),
                adjustment.getReason(),
                adjustment.getApprovedBy(),
                adjustment.getApprovedDate(),
                adjustment.getIsApproved(),
                adjustment.getRemarks(),
                stockAdjustmentId
        );
    }


    public int deleteItems(UUID stockAdjustmentId) {

        String sql = """
                DELETE FROM stock_adjustment_item
                WHERE stock_adjustment_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                stockAdjustmentId
        );
    }
}