-- Adds designations for site/technical staff (selectable in hr/employees/new).
INSERT INTO designation (designationname) VALUES
	('Technical Officer'),
	('Engineer'),
	('QS'),
	('QA')
ON CONFLICT (designationname) DO NOTHING;
