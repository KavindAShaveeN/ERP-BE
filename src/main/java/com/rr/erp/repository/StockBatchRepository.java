package com.rr.erp.repository;

import com.rr.erp.dto.StockBatchView;
import com.rr.erp.dto.StockSizeBreakdown;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.entity.StockBatch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StockBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public StockBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StockBatch> batchRowMapper = (rs, rowNum) -> {
        StockBatch batch = new StockBatch();

        batch.setStockBatchId(rs.getObject("stock_batch_id", UUID.class));
        batch.setBatchCode(rs.getString("batch_code"));
        batch.setItemCode(rs.getString("item_code"));
        batch.setOriginDate(rs.getObject("origin_date", LocalDate.class));
        batch.setSourceType(rs.getString("source_type"));
        batch.setSourceId(rs.getObject("source_id", UUID.class));
        batch.setSourceReference(rs.getString("source_reference"));
        batch.setOriginalQty(rs.getBigDecimal("original_qty"));
        batch.setUnitCost(rs.getBigDecimal("unit_cost"));
        batch.setLengthValue(rs.getBigDecimal("length_value"));
        batch.setWidthValue(rs.getBigDecimal("width_value"));
        batch.setPieceCount(rs.getObject("piece_count", Integer.class));
        batch.setDescription(rs.getString("description"));
        batch.setExpiryDate(rs.getObject("expiry_date", LocalDate.class));

        return batch;
    };

    /** A batch joined to one of its locations — used both for the FIFO walk and the Stock page view. */
    private final RowMapper<StockBatchView> batchViewRowMapper = (rs, rowNum) -> {
        StockBatchView view = new StockBatchView();

        view.setStockBatchId(rs.getObject("stock_batch_id", UUID.class));
        view.setBatchCode(rs.getString("batch_code"));
        view.setItemCode(rs.getString("item_code"));
        view.setOriginDate(rs.getObject("origin_date", LocalDate.class));
        view.setSourceType(rs.getString("source_type"));
        view.setSourceReference(rs.getString("source_reference"));
        view.setUnitCost(rs.getBigDecimal("unit_cost"));
        view.setProjectCode(rs.getString("project_code"));
        view.setQtyRemaining(rs.getBigDecimal("qty_remaining"));
        view.setLengthM(rs.getBigDecimal("length_value"));
        view.setWidthM(rs.getBigDecimal("width_value"));
        view.setPieceCount(rs.getObject("piece_count", Integer.class));
        view.setPieceCountRemaining(rs.getObject("piece_count_remaining", Integer.class));
        view.setDescription(rs.getString("description"));
        view.setExpiryDate(rs.getObject("expiry_date", LocalDate.class));

        return view;
    };

    private final RowMapper<StockSizeBreakdown> sizeBreakdownRowMapper = (rs, rowNum) -> {
        StockSizeBreakdown breakdown = new StockSizeBreakdown();

        breakdown.setLengthM(rs.getBigDecimal("length_value"));
        breakdown.setWidthM(rs.getBigDecimal("width_value"));
        breakdown.setTotalQtyRemaining(rs.getBigDecimal("total_qty_remaining"));
        breakdown.setTotalPieceCountRemaining(rs.getObject("total_piece_count_remaining", Integer.class));

        return breakdown;
    };

    private final RowMapper<StockActionAllocation> allocationRowMapper = (rs, rowNum) -> {
        StockActionAllocation allocation = new StockActionAllocation();

        allocation.setStockActionAllocationId(rs.getObject("stock_action_allocation_id", UUID.class));
        allocation.setStockBatchId(rs.getObject("stock_batch_id", UUID.class));
        allocation.setActionType(rs.getString("action_type"));
        allocation.setActionId(rs.getObject("action_id", UUID.class));
        allocation.setActionItemId(rs.getObject("action_item_id", UUID.class));
        allocation.setItemCode(rs.getString("item_code"));
        allocation.setQtyTaken(rs.getBigDecimal("qty_taken"));
        allocation.setUnitCost(rs.getBigDecimal("unit_cost"));

        return allocation;
    };

    public void insertBatch(StockBatch batch) {

        String sql = """
                INSERT INTO stock_batch (
                    stock_batch_id,
                    batch_code,
                    item_code,
                    origin_date,
                    source_type,
                    source_id,
                    source_reference,
                    original_qty,
                    unit_cost,
                    length_value,
                    width_value,
                    piece_count,
                    description,
                    expiry_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                batch.getStockBatchId(),
                batch.getBatchCode(),
                batch.getItemCode(),
                batch.getOriginDate(),
                batch.getSourceType(),
                batch.getSourceId(),
                batch.getSourceReference(),
                batch.getOriginalQty(),
                batch.getUnitCost(),
                batch.getLengthValue(),
                batch.getWidthValue(),
                batch.getPieceCount(),
                batch.getDescription(),
                batch.getExpiryDate()
        );
    }

    /**
     * Adds {@code qty} to a batch's location row at {@code projectCode},
     * creating the row if the batch has never been at that project before.
     * Used both to place a newly opened batch at its first project, and to
     * credit an existing batch when a transfer/return moves it elsewhere.
     */
    public void creditLocation(UUID stockBatchId, String projectCode, BigDecimal qty) {
        creditLocation(stockBatchId, projectCode, qty, null);
    }

    /**
     * Piece-count-aware variant, for dimensional items (see
     * com.rr.erp.util.DimensionalItems): also sets/increments piece_count_remaining
     * alongside qty_remaining. {@code pieceCount} is null for non-dimensional items,
     * in which case this behaves exactly like the plain overload.
     */
    public void creditLocation(UUID stockBatchId, String projectCode, BigDecimal qty, Integer pieceCount) {

        String sql = """
                INSERT INTO stock_batch_location (stock_batch_id, project_code, qty_remaining, piece_count_remaining)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (stock_batch_id, project_code)
                DO UPDATE SET
                    qty_remaining = stock_batch_location.qty_remaining + EXCLUDED.qty_remaining,
                    piece_count_remaining = COALESCE(stock_batch_location.piece_count_remaining, 0) + COALESCE(EXCLUDED.piece_count_remaining, 0)
                """;

        jdbcTemplate.update(sql, stockBatchId, projectCode, qty, pieceCount);
    }

    public void decrementLocation(UUID stockBatchId, String projectCode, BigDecimal qty) {
        decrementLocation(stockBatchId, projectCode, qty, null);
    }

    /**
     * Piece-count-aware variant, for dimensional items: also decrements
     * piece_count_remaining by {@code piecesTaken} alongside qty_remaining.
     * {@code piecesTaken} is null when this batch isn't dimensional (piece_count
     * IS NULL on stock_batch), in which case only qty_remaining is touched.
     */
    public void decrementLocation(UUID stockBatchId, String projectCode, BigDecimal qty, Integer piecesTaken) {

        String sql = """
                UPDATE stock_batch_location
                SET qty_remaining = qty_remaining - ?,
                    piece_count_remaining = CASE
                        WHEN CAST(? AS INTEGER) IS NULL THEN piece_count_remaining
                        ELSE piece_count_remaining - CAST(? AS INTEGER)
                    END
                WHERE stock_batch_id = ?
                  AND project_code = ?
                """;

        jdbcTemplate.update(sql, qty, piecesTaken, piecesTaken, stockBatchId, projectCode);
    }

    /**
     * This project's batches for this item with stock left, oldest lot first
     * (by the batch's real origin date — even if it only arrived at this
     * project recently via a transfer, its FIFO age is when it was first
     * received into the system). Locks the location rows for the duration
     * of the caller's transaction so concurrent issues can't both allocate
     * from the same remaining quantity.
     * Same-day ties break on batch_code (its zero-padded per-item-per-day
     * sequence — see StockBatchService#buildBatchCode — is assigned in true
     * creation order), not stock_batch_id: that's a random UUID with no
     * relation to receipt order, which previously let same-day ties resolve
     * in an arbitrary sequence different from what the batch codes imply.
     */
    public List<StockBatchView> lockFifoBatchLocations(String projectCode, String itemCode) {
        return lockFifoBatchLocations(projectCode, itemCode, null, null);
    }

    /**
     * Size-scoped variant, for dimensional items (see com.rr.erp.util.DimensionalItems):
     * when {@code lengthValue} is non-null, only draws from batches of that exact size
     * (length AND width, width compared with IS NOT DISTINCT FROM since it may be null
     * for length-only items) — e.g. issuing from the 6m bars must never accidentally
     * consume the 1m offcuts. When {@code lengthValue} is null, behaves exactly like the
     * unscoped overload, so non-dimensional items are unaffected.
     */
    public List<StockBatchView> lockFifoBatchLocations(
            String projectCode, String itemCode, BigDecimal lengthValue, BigDecimal widthValue
    ) {

        String sql = """
                SELECT
                    b.stock_batch_id,
                    b.batch_code,
                    b.item_code,
                    b.origin_date,
                    b.source_type,
                    b.source_reference,
                    b.unit_cost,
                    b.length_value,
                    b.width_value,
                    b.piece_count,
                    b.description,
                    b.expiry_date,
                    loc.project_code,
                    loc.qty_remaining,
                    loc.piece_count_remaining
                FROM stock_batch_location loc
                JOIN stock_batch b ON b.stock_batch_id = loc.stock_batch_id
                WHERE loc.project_code = ?
                  AND b.item_code = ?
                  AND loc.qty_remaining > 0
                  AND (CAST(? AS NUMERIC) IS NULL OR b.length_value = CAST(? AS NUMERIC))
                  AND (CAST(? AS NUMERIC) IS NULL OR b.width_value IS NOT DISTINCT FROM CAST(? AS NUMERIC))
                ORDER BY b.origin_date, b.batch_code
                FOR UPDATE OF loc
                """;

        return jdbcTemplate.query(
                sql, batchViewRowMapper,
                projectCode, itemCode,
                lengthValue, lengthValue,
                widthValue, widthValue
        );
    }

    public void insertAllocation(StockActionAllocation allocation) {

        String sql = """
                INSERT INTO stock_action_allocation (
                    stock_action_allocation_id,
                    stock_batch_id,
                    action_type,
                    action_id,
                    action_item_id,
                    item_code,
                    qty_taken,
                    unit_cost
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                allocation.getStockActionAllocationId(),
                allocation.getStockBatchId(),
                allocation.getActionType(),
                allocation.getActionId(),
                allocation.getActionItemId(),
                allocation.getItemCode(),
                allocation.getQtyTaken(),
                allocation.getUnitCost()
        );
    }

    /** Every batch ever located at this project for this item, oldest first — the full FIFO trail, for the Stock page. */
    public List<StockBatchView> findBatchViewsForItem(String projectCode, String itemCode) {

        String sql = """
                SELECT
                    b.stock_batch_id,
                    b.batch_code,
                    b.item_code,
                    b.origin_date,
                    b.source_type,
                    b.source_reference,
                    b.unit_cost,
                    b.length_value,
                    b.width_value,
                    b.piece_count,
                    b.description,
                    b.expiry_date,
                    loc.project_code,
                    loc.qty_remaining,
                    loc.piece_count_remaining
                FROM stock_batch_location loc
                JOIN stock_batch b ON b.stock_batch_id = loc.stock_batch_id
                WHERE loc.project_code = ?
                  AND b.item_code = ?
                ORDER BY b.origin_date, b.batch_code
                """;

        return jdbcTemplate.query(sql, batchViewRowMapper, projectCode, itemCode);
    }

    /**
     * Per-size stock breakdown for one project/item (e.g. "6m x8, 1m x4, 2m x1") — groups
     * every location row for this item by the batch's (length_value, width_value), summing
     * remaining quantity and piece count across however many batches of that exact size
     * exist. Dimensional items only; for a non-dimensional item this returns a single row
     * with null length/width and the item's plain total quantity.
     */
    public List<StockSizeBreakdown> findSizeBreakdownForItem(String projectCode, String itemCode) {

        String sql = """
                SELECT
                    b.length_value,
                    b.width_value,
                    SUM(loc.qty_remaining) AS total_qty_remaining,
                    SUM(loc.piece_count_remaining) AS total_piece_count_remaining
                FROM stock_batch_location loc
                JOIN stock_batch b ON b.stock_batch_id = loc.stock_batch_id
                WHERE loc.project_code = ?
                  AND b.item_code = ?
                  AND loc.qty_remaining > 0
                GROUP BY b.length_value, b.width_value
                ORDER BY b.length_value NULLS LAST, b.width_value NULLS LAST
                """;

        return jdbcTemplate.query(sql, sizeBreakdownRowMapper, projectCode, itemCode);
    }

    /** One batch by id — used by cut-return processing to inspect the source batch's own size. */
    public Optional<StockBatch> findById(UUID stockBatchId) {

        String sql = """
                SELECT *
                FROM stock_batch
                WHERE stock_batch_id = ?
                """;

        List<StockBatch> results = jdbcTemplate.query(sql, batchRowMapper, stockBatchId);

        return results.stream().findFirst();
    }

    public List<StockActionAllocation> findAllocationsForAction(String actionType, UUID actionId) {

        String sql = """
                SELECT *
                FROM stock_action_allocation
                WHERE action_type = ?
                  AND action_id = ?
                """;

        return jdbcTemplate.query(sql, allocationRowMapper, actionType, actionId);
    }

    /**
     * Every allocation ever drawn from one action for this item code, oldest batch first —
     * used to replay a GIN's exact batches onto the receiving project of an internal
     * transfer. The FIFO order matters here: when the receiving GRN reports a smaller
     * quantity than was issued (transit loss), only the oldest of these get replayed.
     */
    public List<StockActionAllocation> findAllocationsForActionAndItem(String actionType, UUID actionId, String itemCode) {

        String sql = """
                SELECT saa.*
                FROM stock_action_allocation saa
                JOIN stock_batch b ON b.stock_batch_id = saa.stock_batch_id
                WHERE saa.action_type = ?
                  AND saa.action_id = ?
                  AND saa.item_code = ?
                ORDER BY b.origin_date, b.batch_code
                """;

        return jdbcTemplate.query(sql, allocationRowMapper, actionType, actionId, itemCode);
    }

    /** Every batch opened by one specific source action (e.g. one plant production record) — used to reverse exactly the batches that action opened. */
    public List<StockBatch> findBatchesBySource(String sourceType, UUID sourceId) {

        String sql = """
                SELECT *
                FROM stock_batch
                WHERE source_type = ?
                  AND source_id = ?
                """;

        return jdbcTemplate.query(sql, batchRowMapper, sourceType, sourceId);
    }

    /** The remaining quantity of one batch at one project — used to verify a batch is untouched before reversing it. */
    public Optional<BigDecimal> getLocationQtyRemaining(UUID stockBatchId, String projectCode) {

        String sql = """
                SELECT qty_remaining
                FROM stock_batch_location
                WHERE stock_batch_id = ?
                  AND project_code = ?
                """;

        List<BigDecimal> results = jdbcTemplate.query(
                sql, (rs, rowNum) -> rs.getBigDecimal("qty_remaining"), stockBatchId, projectCode
        );

        return results.stream().findFirst();
    }

    /** The most recently opened batch anywhere for this item — the fallback cost when no better signal exists (batches are global, not project-scoped). */
    public Optional<StockBatch> findMostRecentBatch(String itemCode) {

        String sql = """
                SELECT *
                FROM stock_batch
                WHERE item_code = ?
                ORDER BY origin_date DESC, batch_code DESC
                LIMIT 1
                """;

        List<StockBatch> results = jdbcTemplate.query(sql, batchRowMapper, itemCode);

        return results.stream().findFirst();
    }

    /**
     * Atomically increments and returns the next sequence number for a
     * (item, origin date) pair — the "-01", "-02" suffix in a batch code,
     * disambiguating two supplier deliveries of the same item on the same day.
     */
    public int nextSequence(String itemCode, LocalDate originDate) {

        String sql = """
                INSERT INTO stock_batch_sequence (item_code, origin_date, last_seq)
                VALUES (?, ?, 1)
                ON CONFLICT (item_code, origin_date)
                DO UPDATE SET last_seq = stock_batch_sequence.last_seq + 1
                RETURNING last_seq
                """;

        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, itemCode, originDate);

        return result != null ? result : 1;
    }
}
