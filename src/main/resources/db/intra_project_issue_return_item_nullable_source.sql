-- A JOB_CARD return line for a spare part removed from the asset has no source issue line, so
-- intra_project_issue_return_item.intra_project_issue_item_id must allow NULL. The live table
-- was created with NOT NULL (see intra_project_issue_return_multi_source.sql), which makes
-- saving such a return fail. The foreign key to intra_project_issue_item is kept.
--
-- Plain JDBC-backed table, not managed by Hibernate. Run this script manually.

ALTER TABLE intra_project_issue_return_item
    ALTER COLUMN intra_project_issue_item_id DROP NOT NULL;
