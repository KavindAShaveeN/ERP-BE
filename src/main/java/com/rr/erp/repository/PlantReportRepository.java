package com.rr.erp.repository;

import com.rr.erp.dto.PlantCostTrendRow;
import com.rr.erp.dto.PlantExpenseSummaryRow;
import com.rr.erp.dto.PlantFinanceSummaryRow;
import com.rr.erp.dto.PlantProductionCostRow;
import com.rr.erp.dto.PlantProductionLineRow;
import com.rr.erp.dto.PlantProductionProfitRow;
import com.rr.erp.dto.PlantRawMaterialCostRow;
import com.rr.erp.dto.PlantStockMovementRow;
import com.rr.erp.dto.PlantStockSummaryRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class PlantReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public PlantReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PlantProductionLineRow> lineRowMapper = (rs, rowNum) -> {
        PlantProductionLineRow row = new PlantProductionLineRow();

        row.setProductionId(rs.getObject("production_id", UUID.class));
        row.setProductionCode(rs.getString("production_code"));
        row.setProductionDate(rs.getObject("production_date", LocalDate.class));
        row.setStatus(rs.getString("status"));
        row.setProjectCode(rs.getString("project_code"));
        row.setRecipeId(rs.getObject("recipe_id", UUID.class));
        row.setRecipeName(rs.getString("recipe_name"));
        row.setLineType(rs.getString("line_type"));
        row.setItemCode(rs.getString("item_code"));
        row.setUomId(rs.getObject("uom_id", Integer.class));
        row.setPlannedQuantity(rs.getBigDecimal("planned_quantity"));
        row.setActualQuantity(rs.getBigDecimal("actual_quantity"));
        row.setIsWaste((Boolean) rs.getObject("is_waste"));

        return row;
    };

    /**
     * Every input and output line of every production, flattened and filtered —
     * backs the daily production, raw-material consumption, finished-goods
     * production, recipe-vs-actual variance, and wastage/loss reports (they
     * differ only by which filters the caller applies, e.g. lineType/isWaste).
     */
    public List<PlantProductionLineRow> findProductionLines(
            String projectCode,
            UUID recipeId,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            String productItemCode,
            String rawMaterialItemCode
    ) {

        String sql = """
                SELECT
                    pp.production_id, pp.production_code, pp.production_date, pp.status,
                    pp.project_code, pp.recipe_id, pr.recipe_name,
                    'INPUT' AS line_type,
                    ppi.item_code, ppi.uom_id, ppi.planned_quantity,
                    ppi.consumed_quantity AS actual_quantity,
                    FALSE AS is_waste
                FROM plant_production_input ppi
                JOIN plant_production pp ON pp.production_id = ppi.production_id
                LEFT JOIN plant_recipe pr ON pr.recipe_id = pp.recipe_id
                WHERE (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (?::uuid IS NULL OR pp.recipe_id = ?::uuid)
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.status = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                  AND (CAST(? AS VARCHAR) IS NULL OR ppi.item_code = CAST(? AS VARCHAR))

                UNION ALL

                SELECT
                    pp.production_id, pp.production_code, pp.production_date, pp.status,
                    pp.project_code, pp.recipe_id, pr.recipe_name,
                    'OUTPUT' AS line_type,
                    ppo.item_code, ppo.uom_id, ppo.planned_quantity,
                    ppo.produced_quantity AS actual_quantity,
                    ppo.is_waste
                FROM plant_production_output ppo
                JOIN plant_production pp ON pp.production_id = ppo.production_id
                LEFT JOIN plant_recipe pr ON pr.recipe_id = pp.recipe_id
                WHERE (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (?::uuid IS NULL OR pp.recipe_id = ?::uuid)
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.status = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                  AND (CAST(? AS VARCHAR) IS NULL OR ppo.item_code = CAST(? AS VARCHAR))

                ORDER BY production_date DESC, production_code DESC
                """;

        return jdbcTemplate.query(
                sql,
                lineRowMapper,
                projectCode, projectCode,
                recipeId, recipeId,
                status, status,
                dateFrom, dateFrom,
                dateTo, dateTo,
                rawMaterialItemCode, rawMaterialItemCode,

                projectCode, projectCode,
                recipeId, recipeId,
                status, status,
                dateFrom, dateFrom,
                dateTo, dateTo,
                productItemCode, productItemCode
        );
    }

    private final RowMapper<PlantStockMovementRow> movementRowMapper = (rs, rowNum) -> {
        PlantStockMovementRow row = new PlantStockMovementRow();

        row.setProjectCode(rs.getString("project_code"));
        row.setItemCode(rs.getString("item_code"));
        row.setMovementType(rs.getString("movement_type"));
        row.setQuantity(rs.getBigDecimal("quantity"));
        row.setUnitCost(rs.getBigDecimal("unit_cost"));
        row.setMovementDate(rs.getObject("movement_date", LocalDate.class));
        row.setProductionCode(rs.getString("production_code"));

        return row;
    };

    /** Every plant-production-sourced debit (raw material) and credit (finished good), for the plant-wise stock movement report. */
    public List<PlantStockMovementRow> findStockMovements(
            String projectCode,
            String itemCode,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    pp.project_code, saa.item_code,
                    'DEBIT' AS movement_type,
                    saa.qty_taken AS quantity, saa.unit_cost,
                    pp.production_date AS movement_date, pp.production_code
                FROM stock_action_allocation saa
                JOIN plant_production pp ON pp.production_id = saa.action_id
                WHERE saa.action_type = 'PLANT_PRODUCTION'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (CAST(? AS VARCHAR) IS NULL OR saa.item_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)

                UNION ALL

                SELECT
                    pp.project_code, sb.item_code,
                    'CREDIT' AS movement_type,
                    sb.original_qty AS quantity, sb.unit_cost,
                    pp.production_date AS movement_date, pp.production_code
                FROM stock_batch sb
                JOIN plant_production pp ON pp.production_id = sb.source_id
                WHERE sb.source_type = 'PLANT_PRODUCTION'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (CAST(? AS VARCHAR) IS NULL OR sb.item_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)

                ORDER BY movement_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                movementRowMapper,
                projectCode, projectCode, itemCode, itemCode, dateFrom, dateFrom, dateTo, dateTo,
                projectCode, projectCode, itemCode, itemCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    private final RowMapper<PlantProductionCostRow> costRowMapper = (rs, rowNum) -> {
        PlantProductionCostRow row = new PlantProductionCostRow();

        row.setProjectCode(rs.getString("project_code"));
        row.setProductionCode(rs.getString("production_code"));
        row.setProductionDate(rs.getObject("production_date", LocalDate.class));
        row.setItemCode(rs.getString("item_code"));
        row.setBatchCode(rs.getString("batch_code"));
        row.setQuantity(rs.getBigDecimal("quantity"));
        row.setUnitCost(rs.getBigDecimal("unit_cost"));
        row.setTotalCost(rs.getBigDecimal("total_cost"));

        return row;
    };

    /** Production cost per finished product — every finished-goods batch a production opened, with its computed unit cost. */
    public List<PlantProductionCostRow> findProductionCosts(
            String projectCode,
            String itemCode,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    pp.project_code, pp.production_code, pp.production_date,
                    sb.item_code, sb.batch_code, sb.original_qty AS quantity, sb.unit_cost,
                    (sb.original_qty * sb.unit_cost) AS total_cost
                FROM stock_batch sb
                JOIN plant_production pp ON pp.production_id = sb.source_id
                WHERE sb.source_type = 'PLANT_PRODUCTION'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (CAST(? AS VARCHAR) IS NULL OR sb.item_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                ORDER BY pp.production_date DESC
                """;

        return jdbcTemplate.query(
                sql, costRowMapper,
                projectCode, projectCode, itemCode, itemCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    // ---- Finance / management reports (approved productions only) ----

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private final RowMapper<PlantRawMaterialCostRow> rawMaterialCostRowMapper = (rs, rowNum) -> {
        PlantRawMaterialCostRow row = new PlantRawMaterialCostRow();

        row.setProjectCode(rs.getString("project_code"));
        row.setItemCode(rs.getString("item_code"));
        row.setProductionCount(rs.getInt("production_count"));
        row.setTotalQuantity(rs.getBigDecimal("total_quantity"));
        row.setTotalCost(rs.getBigDecimal("total_cost"));
        row.setAvgUnitCost(rs.getBigDecimal("avg_unit_cost"));
        row.setMinUnitCost(rs.getBigDecimal("min_unit_cost"));
        row.setMaxUnitCost(rs.getBigDecimal("max_unit_cost"));

        return row;
    };

    /** Total quantity and FIFO cost of each raw material consumed per plant, highest spend first. */
    public List<PlantRawMaterialCostRow> findRawMaterialCosts(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    pp.project_code, saa.item_code,
                    COUNT(DISTINCT pp.production_id) AS production_count,
                    SUM(saa.qty_taken) AS total_quantity,
                    SUM(saa.qty_taken * saa.unit_cost) AS total_cost,
                    CASE WHEN SUM(saa.qty_taken) > 0
                         THEN SUM(saa.qty_taken * saa.unit_cost) / SUM(saa.qty_taken)
                         ELSE 0 END AS avg_unit_cost,
                    MIN(saa.unit_cost) AS min_unit_cost,
                    MAX(saa.unit_cost) AS max_unit_cost
                FROM stock_action_allocation saa
                JOIN plant_production pp ON pp.production_id = saa.action_id
                WHERE saa.action_type = 'PLANT_PRODUCTION'
                  AND pp.status = 'APPROVED'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (CAST(? AS VARCHAR) IS NULL OR saa.item_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                GROUP BY pp.project_code, saa.item_code
                ORDER BY total_cost DESC
                """;

        return jdbcTemplate.query(
                sql, rawMaterialCostRowMapper,
                projectCode, projectCode, itemCode, itemCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    private final RowMapper<PlantCostTrendRow> costTrendRowMapper = (rs, rowNum) -> {
        PlantCostTrendRow row = new PlantCostTrendRow();

        row.setProjectCode(rs.getString("project_code"));
        row.setItemCode(rs.getString("item_code"));
        row.setMonth(rs.getObject("month", LocalDate.class));
        row.setProductionCount(rs.getInt("production_count"));
        row.setQuantity(rs.getBigDecimal("quantity"));
        row.setTotalCost(rs.getBigDecimal("total_cost"));
        row.setAvgUnitCost(rs.getBigDecimal("avg_unit_cost"));

        return row;
    };

    /** Quantity-weighted unit cost of each finished product per plant per month, oldest first. */
    public List<PlantCostTrendRow> findCostTrend(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    pp.project_code, sb.item_code,
                    date_trunc('month', pp.production_date)::date AS month,
                    COUNT(DISTINCT pp.production_id) AS production_count,
                    SUM(sb.original_qty) AS quantity,
                    SUM(sb.original_qty * sb.unit_cost) AS total_cost,
                    CASE WHEN SUM(sb.original_qty) > 0
                         THEN SUM(sb.original_qty * sb.unit_cost) / SUM(sb.original_qty)
                         ELSE 0 END AS avg_unit_cost
                FROM stock_batch sb
                JOIN plant_production pp ON pp.production_id = sb.source_id
                WHERE sb.source_type = 'PLANT_PRODUCTION'
                  AND pp.status = 'APPROVED'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (CAST(? AS VARCHAR) IS NULL OR sb.item_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                GROUP BY pp.project_code, sb.item_code, date_trunc('month', pp.production_date)
                ORDER BY sb.item_code, pp.project_code, month
                """;

        return jdbcTemplate.query(
                sql, costTrendRowMapper,
                projectCode, projectCode, itemCode, itemCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    /**
     * One row per approved production with its material cost, other expenses,
     * quantity produced and what has actually been sold ("Outside Sell" GINs
     * drawn from the batches this production opened) — shared by the
     * profit-per-production and plant finance summary reports.
     */
    private static final String PRODUCTION_FINANCE_SQL = """
            SELECT
                pp.production_id, pp.project_code, pp.production_code, pp.production_date,
                COALESCE(mat.cost, 0) AS material_cost,
                COALESCE(ex.amount, 0) AS other_expenses,
                COALESCE(produced.quantity, 0) AS quantity_produced,
                COALESCE(waste.quantity, 0) AS waste_quantity,
                COALESCE(sales.quantity, 0) AS quantity_sold,
                COALESCE(sales.revenue, 0) AS revenue,
                COALESCE(sales.cost, 0) AS cost_of_sold
            FROM plant_production pp
            LEFT JOIN (
                SELECT action_id, SUM(qty_taken * unit_cost) AS cost
                FROM stock_action_allocation
                WHERE action_type = 'PLANT_PRODUCTION'
                GROUP BY action_id
            ) mat ON mat.action_id = pp.production_id
            LEFT JOIN (
                SELECT production_id, SUM(amount) AS amount
                FROM plant_production_expense
                GROUP BY production_id
            ) ex ON ex.production_id = pp.production_id
            LEFT JOIN (
                SELECT source_id, SUM(original_qty) AS quantity
                FROM stock_batch
                WHERE source_type = 'PLANT_PRODUCTION'
                GROUP BY source_id
            ) produced ON produced.source_id = pp.production_id
            LEFT JOIN (
                SELECT production_id, SUM(produced_quantity) AS quantity
                FROM plant_production_output
                WHERE is_waste = TRUE
                GROUP BY production_id
            ) waste ON waste.production_id = pp.production_id
            LEFT JOIN (
                SELECT sb.source_id,
                       SUM(saa.qty_taken) AS quantity,
                       SUM(saa.qty_taken * COALESCE(gi.unit_price, 0)) AS revenue,
                       SUM(saa.qty_taken * saa.unit_cost) AS cost
                FROM stock_action_allocation saa
                JOIN stock_batch sb ON sb.stock_batch_id = saa.stock_batch_id
                                   AND sb.source_type = 'PLANT_PRODUCTION'
                JOIN gin ON gin.gin_id = saa.action_id
                JOIN gin_type gt ON gt.gin_type_id = gin.gin_type_id AND gt.gin_type_name = 'Outside Sell'
                JOIN gin_item gi ON gi.gin_item_id = saa.action_item_id
                WHERE saa.action_type = 'GIN'
                GROUP BY sb.source_id
            ) sales ON sales.source_id = pp.production_id
            WHERE pp.status = 'APPROVED'
              AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
              AND (?::date IS NULL OR pp.production_date >= ?::date)
              AND (?::date IS NULL OR pp.production_date <= ?::date)
            """;

    /** Cost and realised profit inputs of each approved production, newest first. */
    public List<PlantProductionProfitRow> findProductionProfits(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {

        return jdbcTemplate.query(
                PRODUCTION_FINANCE_SQL + " ORDER BY pp.production_date DESC, pp.production_code DESC",
                (rs, rowNum) -> {
                    PlantProductionProfitRow row = new PlantProductionProfitRow();

                    row.setProductionId(rs.getObject("production_id", UUID.class));
                    row.setProjectCode(rs.getString("project_code"));
                    row.setProductionCode(rs.getString("production_code"));
                    row.setProductionDate(rs.getObject("production_date", LocalDate.class));
                    row.setMaterialCost(orZero(rs.getBigDecimal("material_cost")));
                    row.setOtherExpenses(orZero(rs.getBigDecimal("other_expenses")));
                    row.setQuantityProduced(orZero(rs.getBigDecimal("quantity_produced")));
                    row.setQuantitySold(orZero(rs.getBigDecimal("quantity_sold")));
                    row.setRevenue(orZero(rs.getBigDecimal("revenue")));
                    row.setCostOfSold(orZero(rs.getBigDecimal("cost_of_sold")));

                    return row;
                },
                projectCode, projectCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    /** Per-plant totals of material cost, other expenses, output, waste and sales. */
    public List<PlantFinanceSummaryRow> findFinanceSummary(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {

        String sql = "SELECT project_code, COUNT(*) AS production_count,"
                + " SUM(material_cost) AS material_cost, SUM(other_expenses) AS other_expenses,"
                + " SUM(quantity_produced) AS quantity_produced, SUM(waste_quantity) AS waste_quantity,"
                + " SUM(quantity_sold) AS quantity_sold, SUM(revenue) AS revenue,"
                + " SUM(cost_of_sold) AS cost_of_sold"
                + " FROM (" + PRODUCTION_FINANCE_SQL + ") production_finance"
                + " GROUP BY project_code ORDER BY project_code";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    PlantFinanceSummaryRow row = new PlantFinanceSummaryRow();

                    row.setProjectCode(rs.getString("project_code"));
                    row.setProductionCount(rs.getInt("production_count"));
                    row.setMaterialCost(orZero(rs.getBigDecimal("material_cost")));
                    row.setOtherExpenses(orZero(rs.getBigDecimal("other_expenses")));
                    row.setQuantityProduced(orZero(rs.getBigDecimal("quantity_produced")));
                    row.setWasteQuantity(orZero(rs.getBigDecimal("waste_quantity")));
                    row.setQuantitySold(orZero(rs.getBigDecimal("quantity_sold")));
                    row.setRevenue(orZero(rs.getBigDecimal("revenue")));
                    row.setCostOfSold(orZero(rs.getBigDecimal("cost_of_sold")));

                    return row;
                },
                projectCode, projectCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    /** Other (non-material) expenses grouped by description per plant, largest first. */
    public List<PlantExpenseSummaryRow> findExpenseSummary(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    pp.project_code,
                    MIN(TRIM(ex.description)) AS description,
                    COUNT(*) AS entry_count,
                    SUM(ex.amount) AS total_amount
                FROM plant_production_expense ex
                JOIN plant_production pp ON pp.production_id = ex.production_id
                WHERE pp.status = 'APPROVED'
                  AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                  AND (?::date IS NULL OR pp.production_date >= ?::date)
                  AND (?::date IS NULL OR pp.production_date <= ?::date)
                GROUP BY pp.project_code, LOWER(TRIM(ex.description))
                ORDER BY total_amount DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    PlantExpenseSummaryRow row = new PlantExpenseSummaryRow();

                    row.setProjectCode(rs.getString("project_code"));
                    row.setDescription(rs.getString("description"));
                    row.setEntryCount(rs.getInt("entry_count"));
                    row.setTotalAmount(rs.getBigDecimal("total_amount"));

                    return row;
                },
                projectCode, projectCode, dateFrom, dateFrom, dateTo, dateTo
        );
    }

    /**
     * Opening/consumed/produced/closing per item at a plant (project) — see
     * {@link com.rr.erp.dto.PlantStockSummaryRow} for the approximation this
     * relies on (closing is exact; opening is derived from it).
     */
    public List<PlantStockSummaryRow> findStockSummary(
            String projectCode,
            String itemCode,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        String sql = """
                SELECT
                    item_code,
                    COALESCE(SUM(consumed), 0) AS consumed_quantity,
                    COALESCE(SUM(produced), 0) AS produced_quantity,
                    COALESCE(closing.quantity_on_hand, 0) AS closing_quantity
                FROM (
                    SELECT ppi.item_code, ppi.consumed_quantity AS consumed, 0 AS produced,
                           pp.project_code
                    FROM plant_production_input ppi
                    JOIN plant_production pp ON pp.production_id = ppi.production_id
                    WHERE pp.status = 'APPROVED'
                      AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                      AND (?::date IS NULL OR pp.production_date >= ?::date)
                      AND (?::date IS NULL OR pp.production_date <= ?::date)

                    UNION ALL

                    SELECT ppo.item_code, 0 AS consumed, ppo.produced_quantity AS produced,
                           pp.project_code
                    FROM plant_production_output ppo
                    JOIN plant_production pp ON pp.production_id = ppo.production_id
                    WHERE pp.status = 'APPROVED'
                      AND ppo.is_waste = FALSE
                      AND (CAST(? AS VARCHAR) IS NULL OR pp.project_code = CAST(? AS VARCHAR))
                      AND (?::date IS NULL OR pp.production_date >= ?::date)
                      AND (?::date IS NULL OR pp.production_date <= ?::date)
                ) movements
                LEFT JOIN project_store closing
                    ON closing.item_code = movements.item_code
                   AND closing.project_code = movements.project_code
                WHERE (CAST(? AS VARCHAR) IS NULL OR movements.item_code = CAST(? AS VARCHAR))
                GROUP BY movements.item_code, closing.quantity_on_hand
                ORDER BY movements.item_code
                """;

        List<PlantStockSummaryRow> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    PlantStockSummaryRow row = new PlantStockSummaryRow();

                    row.setItemCode(rs.getString("item_code"));
                    row.setConsumedQuantity(rs.getBigDecimal("consumed_quantity"));
                    row.setProducedQuantity(rs.getBigDecimal("produced_quantity"));
                    row.setClosingQuantity(rs.getBigDecimal("closing_quantity"));

                    return row;
                },
                projectCode, projectCode, dateFrom, dateFrom, dateTo, dateTo,
                projectCode, projectCode, dateFrom, dateFrom, dateTo, dateTo,
                itemCode, itemCode
        );

        for (PlantStockSummaryRow row : rows) {

            BigDecimal opening = row.getClosingQuantity()
                    .subtract(row.getProducedQuantity())
                    .add(row.getConsumedQuantity());

            row.setOpeningQuantity(opening);
        }

        return rows;
    }
}
