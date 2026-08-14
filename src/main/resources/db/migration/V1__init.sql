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
                           status varchar(16) NOT NULL,
                           version bigint NOT NULL DEFAULT 0
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