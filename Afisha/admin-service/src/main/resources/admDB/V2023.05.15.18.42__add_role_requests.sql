CREATE TABLE role_requests (
id BIGSERIAL NOT NULL,
username VARCHAR (128) unique NOT NULL,
creation_time timestamp not null,
role_id bigint not null,
CONSTRAINT role_requests_pk PRIMARY KEY (id)
);

create table event_approval_requests {
id BIGSERIAL not null,
event_id bigint not null unique,
organizer_id bigint not null,
creation_time timestamp not null,
CONSTRAINT event_approval_requests_pk PRIMARY KEY (id)
}

