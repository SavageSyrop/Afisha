INSERT INTO roles (name) values
('ADMIN'),
('USER');

INSERT INTO permissions (name, role_id) values
('AUTHORIZED_ACTIONS', 1),
('AUTHORIZED_ACTIONS', 2),
('ADMIN_ACTIONS', 1);


INSERT INTO users(username, password, is_open_profile) values
('desertfox', '$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u',true),
('aleoonka', '$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq', false);
