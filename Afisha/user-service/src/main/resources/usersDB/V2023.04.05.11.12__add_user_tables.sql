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
age int NOT NULL,
role_id bigint REFERENCES roles(id),
is_open_profile boolean NOT NULL,
CONSTRAINT users_pk PRIMARY KEY (id)
);





