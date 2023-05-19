INSERT INTO roles (name) values
('ADMIN'),
('USER'),
('ORGANIZER');


INSERT INTO permissions (name, role_id) values
('AUTHORIZED_ACTIONS', 1),
('AUTHORIZED_ACTIONS', 2),
('AUTHORIZED_ACTIONS',3),
('ADMIN_ACTIONS', 1),
('CREATING_ACTIONS',1),
('CREATING_ACTIONS',3);


INSERT INTO users(username, password, email, gender, date_of_birth, role_id, is_open_profile) values
('desertfox', '$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u', 'fess.2002@mail.ru','MALE','01.01.2001', 1, true),
('aleoonka', '$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq', 'fess.2002@mail.ru','FEMALE','02.02.2002', 2, false);

INSERT INTO support_requests(user_id,admin_id,question,answer,creation_time,close_time) values
(2,1,'Where is Tomsk?','In Siberia',TIMESTAMP '2023-05-13 11:54:38',TIMESTAMP '2023-05-15 11:50:00'),
(2,null,'How to find my favorites?',null,TIMESTAMP '2023-05-15 11:56:00',null);
