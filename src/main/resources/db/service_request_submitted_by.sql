-- Records which employee submitted a service request, so the workshop can
-- auto-fill "Informed by" on job cards created from it. Run manually against
-- the application database (this codebase has no Hibernate ddl-auto / Flyway).

ALTER TABLE service_request
    ADD COLUMN IF NOT EXISTS submitted_by VARCHAR(50) REFERENCES employee (employee_code);
