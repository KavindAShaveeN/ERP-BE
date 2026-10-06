-- Transport plan, update 1: follow every GIN until its final GRN, whichever way it leaves a hub.
-- Run once manually AFTER transport_plan_schema.sql (DEV first, then production). Safe to re-run.
--
-- What changes
--   custody_status gains COLLECTED: the GIN left the hub (picked up by the destination's own vehicle, or
--   delivered by the hub's vehicle) and is on its way to the destination. The allocation stays ACTIVE until the
--   destination's GRN is approved, which turns it into DELIVERED. Before this, a GIN released from a hub was
--   closed as HANDED_OVER, so nothing marked it delivered when the GRN was approved and the trip page lost it.
--   HANDED_OVER now only means "moved onto a later planned trip".

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        WHERE c.conrelid = 'transport_trip_gin'::regclass
          AND c.contype = 'c'
          AND pg_get_constraintdef(c.oid) ILIKE '%custody_status%'
    LOOP
        EXECUTE format('ALTER TABLE transport_trip_gin DROP CONSTRAINT %I', constraint_name);
    END LOOP;

    ALTER TABLE transport_trip_gin
        ADD CONSTRAINT transport_trip_gin_custody_status_check
        CHECK (custody_status IN ('ALLOCATED', 'ON_VEHICLE', 'AT_HUB', 'COLLECTED', 'AT_DESTINATION',
                                  'UNDELIVERED', 'DELIVERED', 'HANDED_OVER', 'RETURNED_TO_STORE', 'REMOVED'));
END $$;

-- Repair GINs that were released from a hub under the old rule (closed as HANDED_OVER with nothing after them):
--   * their destination's GRN is already approved  -> DELIVERED (and a DELIVERED event, so the trail is complete)
--   * otherwise                                    -> COLLECTED and active again, so the GRN will close them
-- Only rows whose latest hub event was a collection, and that no later allocation continues, are touched.

WITH orphaned AS (
    SELECT a.allocation_id, a.gin_id, a.trip_id, g.received_project_code,
           EXISTS (SELECT 1 FROM grn r WHERE r.gin_id = a.gin_id AND r.is_approved = TRUE) AS grn_approved
    FROM transport_trip_gin a
    JOIN gin g ON g.gin_id = a.gin_id
    WHERE a.custody_status = 'HANDED_OVER'
      AND a.is_active = FALSE
      AND NOT EXISTS (SELECT 1 FROM transport_trip_gin n WHERE n.previous_allocation_id = a.allocation_id)
      AND EXISTS (SELECT 1 FROM gin_custody_event e
                  WHERE e.gin_id = a.gin_id AND e.trip_id = a.trip_id AND e.event_type = 'COLLECTED_FROM_HUB')
      AND NOT EXISTS (SELECT 1 FROM transport_trip_gin x WHERE x.gin_id = a.gin_id AND x.is_active = TRUE)
),
delivered AS (
    UPDATE transport_trip_gin a
    SET custody_status = 'DELIVERED', is_active = FALSE, updated_at = CURRENT_TIMESTAMP
    FROM orphaned o
    WHERE a.allocation_id = o.allocation_id AND o.grn_approved
    RETURNING a.gin_id, a.trip_id, o.received_project_code
),
reopened AS (
    UPDATE transport_trip_gin a
    SET custody_status = 'COLLECTED', is_active = TRUE, updated_at = CURRENT_TIMESTAMP
    FROM orphaned o
    WHERE a.allocation_id = o.allocation_id AND NOT o.grn_approved
    RETURNING a.gin_id
)
INSERT INTO gin_custody_event (event_id, gin_id, trip_id, event_type, location_project_code, remarks)
SELECT gen_random_uuid(), d.gin_id, d.trip_id, 'DELIVERED', d.received_project_code,
       'GRN approved (recorded by the transport plan update)'
FROM delivered d
WHERE NOT EXISTS (SELECT 1 FROM gin_custody_event e WHERE e.gin_id = d.gin_id AND e.event_type = 'DELIVERED');
