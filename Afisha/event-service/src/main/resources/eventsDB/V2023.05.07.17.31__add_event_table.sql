CREATE TYPE participation_type as enum (
    'ORGANIZER',
    'FAVORITE'
);

CREATE TYPE event_type as enum (
    'MOVIE',
    'THEATER',
    'CONCERT',
    'EXPOSITION',
    'OTHER'
);

CREATE TABLE event_participations (
id BIGSERIAL NOT NULL,
user_id bigint NOT NULL,
event_id bigint NOT NULL references events(id) ON DELETE CASCADE,
participation participation_type NOT NULL,
CONSTRAINT event_participations_pk PRIMARY KEY (id)
);

create table events (
id BIGSERIAL NOT NULL,
organizer_id bigint NOT NULL,
type event_type NOT NULL,
name varchar (128) not null,
info varchar (255) not null,
price int not null default 0,
start_time timestamp NOT NULL,
location varchar (128) NOT NULL,
rating float not null default 0,
is_accepted boolean default false,
CONSTRAINT events_pk PRIMARY KEY (id)
);

create table event_votes (
id bigserial not null,
user_id bigint not null,
event_id bigint not null references events(id) ON DELETE CASCADE,
vote_value smallint not null CONSTRAINT vote_border CHECK (vote_value > 0 AND vote_value<11),
CONSTRAINT event_votes_pk PRIMARY KEY (id)
);

create table event_comments (
id bigserial not null,
user_id bigint not null,
event_id bigint not null references events(id) ON DELETE CASCADE,
comment varchar (255) not null,
creation_time timestamp not null default now(),
CONSTRAINT event_comments_pk PRIMARY KEY (id)
);


