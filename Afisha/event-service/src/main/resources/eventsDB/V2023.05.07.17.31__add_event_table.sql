CREATE TYPE participation_type as enum {
    'ORGANIZER',
    'FAVORITE'
}

CREATE TYPE event_type as enum {
    'MOVIE',
    'THEATER',
    'CONCERT',
    'EXPOSITION'
}

CREATE TABLE event_participation (
id BIGSERIAL NOT NULL,
user_id bigint NOT NULL,
event_id bigint NOT NULL,
participation participation_type NOT NULL
);

create table event {
id BIGSERIAL NOT NULL,
organiser_id bigint NOT NULL,
type event_type NOT NULL,
info varchar (255) not null,
price int not null default 0,
start_time timestamp NOT NULL,
location varchar (128) NOT NULL,
rating float not null default 0
}

create table event_vote {
id bigserial not null,
user_id bigint not null
}

create table event_comment {
id bigserial not null,
user_id bigint not null,
comment varchar (255) not null,
creation_time timestamp not null default now()
}


