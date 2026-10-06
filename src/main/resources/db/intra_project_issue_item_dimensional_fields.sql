-- Lets an intra-project issue line for a dimensional item (see
-- com.rr.erp.util.DimensionalItems) record which exact size is being issued
-- to the work location/subcontractor/employee, so the size-aware FIFO draw
-- (StockBatchService#issueFifo) only consumes batches of that size. The
-- existing free-text `size` column is left untouched.
--
-- Plain JDBC-backed table (see intra_project_issue_schema.sql). Run this
-- script manually against the application database.

ALTER TABLE intra_project_issue_item
    ADD COLUMN IF NOT EXISTS length_m NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_m  NUMERIC(10,3);
