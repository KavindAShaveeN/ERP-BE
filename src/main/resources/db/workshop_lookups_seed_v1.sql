-- Workshop departments and job statuses (explicit ids so they match across environments).
INSERT INTO workshop_department (workshop_department_id, workshop_department_name) VALUES
	(1, 'Painting'),
	(2, 'Mechanical'),
	(3, 'Electrical'),
	(4, 'Welding, Lathe and Tinkering'),
	(5, 'General'),
	(6, 'AC')
ON CONFLICT DO NOTHING;

INSERT INTO job_status_type (job_status_type_id, job_status_type_name) VALUES
	(1, 'Pending'),
	(2, 'Approved'),
	(3, 'Assigned'),
	(4, 'In Progress'),
	(5, 'On Hold'),
	(6, 'Completed'),
	(7, 'Cancelled')
ON CONFLICT DO NOTHING;

-- Explicit ids don't advance the serial sequences; keep later inserts from colliding.
SELECT setval(pg_get_serial_sequence('workshop_department', 'workshop_department_id'),
	GREATEST((SELECT MAX(workshop_department_id) FROM workshop_department), 1));
SELECT setval(pg_get_serial_sequence('job_status_type', 'job_status_type_id'),
	GREATEST((SELECT MAX(job_status_type_id) FROM job_status_type), 1));
