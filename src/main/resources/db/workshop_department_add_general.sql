-- Adds a "General" workshop department (for jobs that don't belong to one specific
-- trade department) alongside Painting, Mechanical, Electrical and Welding & Lathe.
-- Run manually against the application database (this codebase has no Hibernate
-- ddl-auto / Flyway — see the other scripts in this folder).

INSERT INTO workshop_department (workshop_department_name) VALUES
    ('General')
ON CONFLICT (workshop_department_name) DO NOTHING;
