-- Lets an intra-project issue return line record the RESULT size being
-- returned — for an ordinary return this is the same size the item was
-- issued as; for a cut-return (worker cut the issued bars on site and is
-- returning offcuts of a NEW size) it differs from the original issue
-- line's size, which is exactly how IntraProjectIssueReturnService tells the
-- two paths apart (compares this line's length_m/width_m against the source
-- intra_project_issue_item's length_m/width_m). See also stock_batch_split
-- (stock_batch_dimensional_tracking.sql), which records the actual cut
-- transformation once a cut-return line is applied to stock. The existing
-- free-text `size` column is left untouched.
--
-- Plain JDBC-backed table (see intra_project_issue_return_schema.sql). Run
-- this script manually against the application database.

ALTER TABLE intra_project_issue_return_item
    ADD COLUMN IF NOT EXISTS length_m NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_m  NUMERIC(10,3);
