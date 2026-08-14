CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
                       id uuid PRIMARY KEY,
                       name varchar(255) NOT NULL,
                       email varchar(255) NOT NULL UNIQUE
);

CREATE TABLE time_slot (
                           id uuid PRIMARY KEY,
                           owner_id uuid NOT NULL REFERENCES users(id),
                           start_ts timestamptz NOT NULL,
                           end_ts timestamptz NOT NULL,
                           status varchar(16) NOT NULL CHECK (status IN ('FREE', 'BUSY', 'BOOKED')),
                           version bigint NOT NULL DEFAULT 0,
                           CONSTRAINT time_slot_valid_range CHECK (start_ts < end_ts)
);

CREATE INDEX idx_time_slot_owner_range
    ON time_slot (owner_id, start_ts, end_ts);

ALTER TABLE time_slot
    ADD CONSTRAINT time_slot_no_overlap
    EXCLUDE USING gist (
        owner_id WITH =,
        tstzrange(start_ts, end_ts) WITH &&
    );

CREATE TABLE meeting (
                         id uuid PRIMARY KEY,
                         slot_id uuid NOT NULL UNIQUE REFERENCES time_slot(id),
                         title varchar(200) NOT NULL,
                         description text
);

CREATE TABLE meeting_participant (
                                     meeting_id uuid NOT NULL REFERENCES meeting(id) ON DELETE CASCADE,
                                     name varchar(255) NOT NULL,
                                     email varchar(255) NOT NULL
);
