-- Correct catalogue spelling and release the one legacy report that used the correct spelling.
BEGIN;
UPDATE fault_type SET label='Stepping motor defect' WHERE fault_code='FAULT_034' AND label='Stepin motor defect';
UPDATE service_request_fault f SET fault_code='FAULT_034', review_required=false
WHERE description='Stepping motor defect' AND fault_code IS NULL AND review_required
  AND NOT EXISTS (SELECT 1 FROM job_card j WHERE j.service_request_id=f.service_request_id)
  AND NOT EXISTS (SELECT 1 FROM job_defect_source s WHERE s.request_fault_id=f.request_fault_id);
COMMIT;
