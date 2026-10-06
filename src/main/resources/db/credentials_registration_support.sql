-- Supports the new "register a login for an employee" flow (POST /api/register).
-- Run this once manually against the ERPRR database (there is no Flyway; Hibernate does not create this).
--
-- Safe to re-run: constraints are only added if missing.
--
-- One login per employee, and usernames must be unique. Neither was enforced before this script.
--
-- IMPORTANT: logins now verify against a BCrypt hash (see LoginService), not a plaintext match.
-- Any credentials rows inserted by hand before this change store a plaintext password and will
-- no longer authenticate. There was no registration flow before this change, so these are expected
-- to just be test rows — delete them and re-create the login through POST /api/register.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'credentials_username_unique'
    ) THEN
        ALTER TABLE credentials
            ADD CONSTRAINT credentials_username_unique UNIQUE (userName);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'credentials_employee_code_unique'
    ) THEN
        ALTER TABLE credentials
            ADD CONSTRAINT credentials_employee_code_unique UNIQUE (employee_code);
    END IF;
END $$;
