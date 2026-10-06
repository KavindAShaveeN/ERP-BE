-- Adds the employee who issued a GIN from the store. Plain JDBC-backed table, not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application database.

ALTER TABLE gin
    ADD COLUMN IF NOT EXISTS issued_by VARCHAR(50);

ALTER TABLE gin
    DROP CONSTRAINT IF EXISTS fk_gin_issued_by;

ALTER TABLE gin
    ADD CONSTRAINT fk_gin_issued_by FOREIGN KEY (issued_by) REFERENCES employee(employee_code);
