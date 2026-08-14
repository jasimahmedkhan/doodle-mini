CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE INDEX idx_time_slot_owner_range
    ON time_slot (owner_id, start_ts, end_ts);

ALTER TABLE time_slot
    ADD CONSTRAINT time_slot_no_overlap
    EXCLUDE USING gist (
        owner_id WITH =,
        tstzrange(start_ts, end_ts) WITH &&
    );