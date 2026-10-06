-- Fixed set of workshop departments a job card can be assigned to (Painting, Mechanical,
-- Electrical, Welding & Lathe, General). Referenced from job_card.department by name — see
-- WorkshopDepartmentController / JobCardsPage.tsx / JobCardFormPage.tsx.

CREATE TABLE IF NOT EXISTS workshop_department (
    workshop_department_id   SERIAL        PRIMARY KEY,
    workshop_department_name VARCHAR(60)   NOT NULL UNIQUE
);

INSERT INTO workshop_department (workshop_department_name) VALUES
    ('Painting'),
    ('Mechanical'),
    ('Electrical'),
    ('Welding & Lathe'),
    ('General')
ON CONFLICT (workshop_department_name) DO NOTHING;
