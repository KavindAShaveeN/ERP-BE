-- Records every login attempt (successful or failed) and, when the frontend reports it via
-- POST /api/logout, the matching logout time. Plain JDBC-backed table, not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

CREATE TABLE IF NOT EXISTS login_history (
    login_history_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_name        varchar(100) NOT NULL,
    employee_code    varchar(50)  NULL,
    login_status     varchar(30)  NOT NULL,
    login_time       timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    logout_time      timestamp    NULL,
    ip_address       varchar(45)  NULL,
    user_agent       varchar(500) NULL,
    CONSTRAINT login_history_pkey PRIMARY KEY (login_history_id),
    CONSTRAINT login_history_status_check CHECK (login_status IN
        ('SUCCESS', 'FAILED_BAD_PASSWORD', 'FAILED_UNKNOWN_USER')),
    CONSTRAINT fk_login_history_employee FOREIGN KEY (employee_code)
        REFERENCES employee(employee_code)
);

CREATE INDEX IF NOT EXISTS idx_login_history_employee_time ON login_history (employee_code, login_time DESC);
CREATE INDEX IF NOT EXISTS idx_login_history_login_time    ON login_history (login_time DESC);
