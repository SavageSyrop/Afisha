create TABLE chats (
id bigserial PRIMARY KEY NOT NULL,
name varchar (64) NOT NULL
);

create TABLE messages (
id bigserial PRIMARY KEY NOT NULL,
sender_id bigint  NOT NULL,
text varchar (255) NOT NULL,
sending_time TIMESTAMP NOT NULL,
chat_id bigint REFERENCES chats(id) not null
);


create TABLE chat_participations (
id bigserial PRIMARY KEY NOT NULL,
user_id bigint not null,
chat_id bigint not null
);


