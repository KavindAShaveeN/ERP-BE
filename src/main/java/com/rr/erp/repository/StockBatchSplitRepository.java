package com.rr.erp.repository;

import com.rr.erp.entity.StockBatchSplit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StockBatchSplitRepository {

    private final JdbcTemplate jdbcTemplate;

    public StockBatchSplitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(StockBatchSplit split) {

        String sql = """
                INSERT INTO stock_batch_split (
                    stock_batch_split_id,
                    intra_project_issue_return_id,
                    source_stock_batch_id,
                    source_piece_count_consumed,
                    source_qty_consumed,
                    result_stock_batch_id,
                    result_qty_produced,
                    wastage_qty
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                split.getStockBatchSplitId(),
                split.getIntraProjectIssueReturnId(),
                split.getSourceStockBatchId(),
                split.getSourcePieceCountConsumed(),
                split.getSourceQtyConsumed(),
                split.getResultStockBatchId(),
                split.getResultQtyProduced(),
                split.getWastageQty()
        );
    }
}
