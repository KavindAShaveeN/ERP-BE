-- Expands "employee" and "project" to the full field set the frontend collects
-- (ERPRR/src/types/domain.ts Employee/Project), on top of whatever leaner schema
-- may already exist. Purely additive (CREATE TABLE IF NOT EXISTS / ADD COLUMN IF NOT
-- EXISTS) — safe to run whether these tables already exist (with the old, leaner
-- column set) or not at all yet.
--
-- Note: designation_id / employee_status_id / project_status_id / project_type_id
-- (if present from an older version of these tables) are left in place but are no
-- longer required or written to by the current EmployeeService/ProjectService — the
-- new plain designation / employment_status / project_status / project_type string
-- columns are the ones now used, matching how the frontend already models them
-- (see EmployeeRepository.java / ProjectRepository.java). Any NOT NULL constraint on
-- those old id columns is dropped below so old-schema installs keep working.

DO $$
BEGIN
    IF to_regclass('public.employee') IS NULL THEN
        CREATE TABLE employee (
            employee_code VARCHAR(30) PRIMARY KEY
        );
    END IF;
END $$;

ALTER TABLE employee ADD COLUMN IF NOT EXISTS employee_type                    VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS temporary_y_number               VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS epf_number                       VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS employment_status                VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS joined_date                      DATE;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS permanent_appointment_date       DATE;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS department                       VARCHAR(80);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS designation                      VARCHAR(80);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS work_location                    VARCHAR(120);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS reporting_supervisor             VARCHAR(30);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS nic_number                       VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS full_name                        VARCHAR(120);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS name_with_initials               VARCHAR(60);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS date_of_birth                    DATE;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS gender                          VARCHAR(10);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS marital_status                   VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS nationality                     VARCHAR(60);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS contact_number                   VARCHAR(30);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS alt_contact_number               VARCHAR(30);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_address                    VARCHAR(150);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS residential_address              VARCHAR(250);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS emergency_contact_person         VARCHAR(120);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS emergency_contact_number         VARCHAR(30);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS emergency_contact_relationship   VARCHAR(60);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS bank_name                        VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS bank_branch                      VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS bank_account_number              VARCHAR(60);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS account_holder_name              VARCHAR(120);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS basic_salary                     NUMERIC(14, 2);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS profile_photo                    VARCHAR(300);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS nic_copy                         VARCHAR(300);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS epf_documents                    VARCHAR(300);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS remarks                          VARCHAR(500);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS created_at                       TIMESTAMP DEFAULT now();
ALTER TABLE employee ADD COLUMN IF NOT EXISTS updated_at                       TIMESTAMP DEFAULT now();

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employee' AND column_name = 'designation_id') THEN
        ALTER TABLE employee ALTER COLUMN designation_id DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employee' AND column_name = 'employee_status_id') THEN
        ALTER TABLE employee ALTER COLUMN employee_status_id DROP NOT NULL;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS assigned_projects (
    employee_code VARCHAR(30) NOT NULL REFERENCES employee (employee_code) ON DELETE CASCADE,
    project_id    INT         NOT NULL,
    PRIMARY KEY (employee_code, project_id)
);

-- ---------------------------------------------------------------------------
-- Project table expansion (see ProjectRepository.java / ProjectResponseDTO.java).
-- ---------------------------------------------------------------------------

ALTER TABLE project ADD COLUMN IF NOT EXISTS project_type            VARCHAR(80);
ALTER TABLE project ADD COLUMN IF NOT EXISTS project_status          VARCHAR(20);
ALTER TABLE project ADD COLUMN IF NOT EXISTS client_contact_person   VARCHAR(120);
ALTER TABLE project ADD COLUMN IF NOT EXISTS client_contact_number   VARCHAR(30);
ALTER TABLE project ADD COLUMN IF NOT EXISTS client_email            VARCHAR(120);
ALTER TABLE project ADD COLUMN IF NOT EXISTS remarks                 VARCHAR(500);
ALTER TABLE project ADD COLUMN IF NOT EXISTS assigned_store          VARCHAR(80);
ALTER TABLE project ADD COLUMN IF NOT EXISTS created_at              TIMESTAMP DEFAULT now();
ALTER TABLE project ADD COLUMN IF NOT EXISTS updated_at              TIMESTAMP DEFAULT now();

-- Every other column in this table was created via unquoted identifiers too (see
-- ProjectRepository.java's SQL — projectStatusId, project_Code, etc.), which Postgres
-- folds to lowercase, so the real stored name is "projectstatusid", not "projectStatusId".
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'project' AND column_name = 'projectstatusid') THEN
        ALTER TABLE project ALTER COLUMN projectstatusid DROP NOT NULL;
    END IF;
END $$;
