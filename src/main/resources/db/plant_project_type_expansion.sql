-- Adds the "Plant" project type (Batching/Crusher/Asphalt/Sand plants), which unlike a
-- normal contracted project has no client/contract value/expected finish date — those
-- are collected only for other project types (see ProjectFormPage.tsx). Purely additive,
-- safe to run against an existing database.

ALTER TABLE project ADD COLUMN IF NOT EXISTS plant_type VARCHAR(40);

INSERT INTO project_type (project_type_name)
SELECT 'Plant'
WHERE NOT EXISTS (
    SELECT 1 FROM project_type WHERE project_type_name = 'Plant'
);
