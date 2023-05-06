CREATE TABLE roles (
id BIGSERIAL NOT NULL,
name VARCHAR (128) NOT NULL,
CONSTRAINT roles_pk PRIMARY KEY (id)
);

CREATE TABLE permissions (
id BIGSERIAL NOT NULL,
name VARCHAR (128) NOT NULL,
role_id bigint NOT NULL REFERENCES roles(id) ON DELETE NO ACTION,
CONSTRAINT permissions_pk PRIMARY KEY (id)
);

CREATE TABLE users (
id BIGSERIAL NOT NULL,
username VARCHAR (128) UNIQUE NOT NULL,
password VARCHAR (128) NOT NULL,
email VARCHAR (128) NOT NULL,
gender_type varchar (64) NOT NULL,
date_of_birth timestamp NOT NULL,
role_id bigint REFERENCES roles(id),
is_open_profile boolean NOT NULL,
activation_code varchar (128) default null,
restore_password_code varchar (128) default null,
is_banned boolean default false,
CONSTRAINT users_pk PRIMARY KEY (id)
);


CrEATE TABLE support_questions (
id BIGSERIAL NOT NULL,
user_id bigint REFERENCES users(id) NOT NULL,
admin_id bigint REFERENCES users(id),
question varchar (128) NOT NULL,
answer varchar (128),
status varchar (128) NOT NULL,
creation_time timestamp NOT NULL,
close_time timestamp,
CONSTRAINT support_questions_pk PRIMARY KEY (id)
)