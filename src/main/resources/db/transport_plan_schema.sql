-- Transport plan: vehicle pool, trips, stops, GIN allocation and custody trail.
-- Run this once manually against the ERPRR database (there is no Flyway; Hibernate does not create these).
-- Run it on DEV first and verify before running on production.
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).
--
-- Model
--   transport_vehicle     the vehicle pool: registered Vehicle assets (asset code only) that may be put on a trip.
--                         A vehicle's status (Available / Allocated / In transit / Returning / Maintenance) is NOT
--                         stored: it is derived on read from its active trip, its asset status and open job cards.
--   transport_trip        one vehicle run from an origin project, through ordered stops, back to a base project.
--   transport_trip_stop   ordered stops of a trip: HUB (holds goods in custody, no GRN), SITE (a GIN destination)
--                         or RETURN_TO_BASE (the last stop; the store closes the trip here).
--   transport_trip_gin    one GIN allocated to a trip, DIRECT (unloaded at its destination) or VIA_HUB (unloaded at a
--                         hub project, then handed to a later local trip). A GIN has at most one ACTIVE allocation.
--   gin_custody_event     audit trail of where each GIN physically was and who handled it.
--   asset_location.trip_id the trip that caused a location entry (vehicle stop entries and HUB_HOLD entries).
--
-- asset_location.movement_type gains HUB_HOLD: an asset on a GIN that is parked at a hub project. It is still IN
-- TRANSIT (the GRN at the destination closes it), exactly like DISPATCH. new_location stays the destination project
-- (so the GRN receipt validation is unchanged); from_location is the hub project it is held at.

CREATE SEQUENCE IF NOT EXISTS transport_trip_code_seq START 1;

CREATE TABLE IF NOT EXISTS transport_vehicle (
    asset_code      VARCHAR(50)  PRIMARY KEY REFERENCES asset (asset_code),
    default_driver  VARCHAR(150),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    added_by        VARCHAR(50)  REFERENCES employee (employee_code),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transport_trip (
    trip_id              UUID         PRIMARY KEY,
    trip_code            VARCHAR(30)  NOT NULL UNIQUE,
    vehicle_asset_code   VARCHAR(50)  NOT NULL REFERENCES transport_vehicle (asset_code),
    driver_name          VARCHAR(150),
    origin_project_code  VARCHAR(150) NOT NULL REFERENCES project (project_code),
    trip_type            VARCHAR(30)  NOT NULL DEFAULT 'DIRECT'
                         CHECK (trip_type IN ('DIRECT', 'HUB_DISTRIBUTION')),
    status               VARCHAR(20)  NOT NULL DEFAULT 'PLANNED'
                         CHECK (status IN ('PLANNED', 'IN_TRANSIT', 'RETURNING', 'COMPLETED', 'CANCELLED')),
    planned_departure    TIMESTAMP,
    actual_departure     TIMESTAMP,
    remarks              VARCHAR(500),
    created_by           VARCHAR(50)  REFERENCES employee (employee_code),
    closed_by            VARCHAR(50)  REFERENCES employee (employee_code),
    closed_at            TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A vehicle can be on only one open trip at a time.
CREATE UNIQUE INDEX IF NOT EXISTS uq_transport_trip_open_vehicle
    ON transport_trip (vehicle_asset_code)
    WHERE status IN ('PLANNED', 'IN_TRANSIT', 'RETURNING');

CREATE INDEX IF NOT EXISTS idx_transport_trip_status ON transport_trip (status);

CREATE TABLE IF NOT EXISTS transport_trip_stop (
    stop_id       UUID         PRIMARY KEY,
    trip_id       UUID         NOT NULL REFERENCES transport_trip (trip_id) ON DELETE CASCADE,
    seq           INTEGER      NOT NULL,
    project_code  VARCHAR(150) NOT NULL REFERENCES project (project_code),
    stop_type     VARCHAR(20)  NOT NULL CHECK (stop_type IN ('HUB', 'SITE', 'RETURN_TO_BASE')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING', 'ARRIVED', 'DONE', 'SKIPPED')),
    eta           TIMESTAMP,                 -- entered by hand, optional
    arrived_at    TIMESTAMP,
    departed_at   TIMESTAMP,
    CONSTRAINT uq_transport_trip_stop_seq UNIQUE (trip_id, seq)
);

CREATE INDEX IF NOT EXISTS idx_transport_trip_stop_trip    ON transport_trip_stop (trip_id);
CREATE INDEX IF NOT EXISTS idx_transport_trip_stop_project ON transport_trip_stop (project_code);

CREATE TABLE IF NOT EXISTS transport_trip_gin (
    allocation_id        UUID         PRIMARY KEY,
    trip_id              UUID         NOT NULL REFERENCES transport_trip (trip_id) ON DELETE CASCADE,
    gin_id               UUID         NOT NULL REFERENCES gin (gin_id),
    delivery_mode        VARCHAR(10)  NOT NULL CHECK (delivery_mode IN ('DIRECT', 'VIA_HUB')),
    drop_stop_id         UUID         REFERENCES transport_trip_stop (stop_id) ON DELETE SET NULL,
    -- ALLOCATED      on a planned trip, not loaded yet
    -- ON_VEHICLE     loaded, the trip has departed
    -- AT_HUB         unloaded at a hub project and held there (no GRN, no stock movement)
    -- COLLECTED      left the hub (destination's vehicle collected it, or the hub's vehicle is delivering it);
    --                stays active until the destination's GRN is approved (see transport_plan_update_1.sql)
    -- AT_DESTINATION unloaded at its destination, waiting for the GRN
    -- UNDELIVERED    the planner marked it not delivered
    -- DELIVERED      the destination's GRN was approved
    -- HANDED_OVER    held at a hub and moved onto a later local trip
    -- RETURNED_TO_STORE came back to the base project when the trip closed
    -- REMOVED        taken off the trip before it left
    custody_status       VARCHAR(20)  NOT NULL DEFAULT 'ALLOCATED'
                         CHECK (custody_status IN ('ALLOCATED', 'ON_VEHICLE', 'AT_HUB', 'COLLECTED',
                                                   'AT_DESTINATION', 'UNDELIVERED', 'DELIVERED', 'HANDED_OVER',
                                                   'RETURNED_TO_STORE', 'REMOVED')),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    previous_allocation_id UUID       REFERENCES transport_trip_gin (allocation_id),
    returned_at_project  VARCHAR(150) REFERENCES project (project_code),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A GIN can have only one active allocation at a time.
CREATE UNIQUE INDEX IF NOT EXISTS uq_transport_trip_gin_active
    ON transport_trip_gin (gin_id)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_transport_trip_gin_trip ON transport_trip_gin (trip_id);
CREATE INDEX IF NOT EXISTS idx_transport_trip_gin_gin  ON transport_trip_gin (gin_id);

CREATE TABLE IF NOT EXISTS gin_custody_event (
    event_id               UUID         PRIMARY KEY,
    gin_id                 UUID         NOT NULL REFERENCES gin (gin_id),
    trip_id                UUID         REFERENCES transport_trip (trip_id) ON DELETE SET NULL,
    event_type             VARCHAR(30)  NOT NULL,
    -- LOADED, UNLOADED_TO_HUB, HANDED_TO_LOCAL_VEHICLE, UNLOADED_AT_DESTINATION, DELIVERED,
    -- NOT_DELIVERED, RETURNED_TO_STORE
    location_project_code  VARCHAR(150) REFERENCES project (project_code),
    performed_by           VARCHAR(50)  REFERENCES employee (employee_code),
    event_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks                VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_gin_custody_event_gin ON gin_custody_event (gin_id, event_time);

-- Which trip produced an asset_location entry (vehicle stop entries, HUB_HOLD entries).
ALTER TABLE asset_location
    ADD COLUMN IF NOT EXISTS trip_id UUID REFERENCES transport_trip (trip_id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_asset_location_trip ON asset_location (trip_id) WHERE trip_id IS NOT NULL;
