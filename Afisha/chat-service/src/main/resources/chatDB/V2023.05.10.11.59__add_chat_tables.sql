create TABLE chats (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
name varchar (64) NOT NULL
);

create TABLE messages (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
sender_id bigint  NOT NULL REFERENCES users(id),
text varchar (255) NOT NULL,
sending_time TIMESTAMP NOT NULL,
chat_id bigint REFERENCES chats(id)
);


create TABLE chat_participations (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
user_id bigint REFERENCES users(id),
chat_id bigint REFERENCES chats(id)
);


