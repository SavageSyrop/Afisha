INSERT INTO events(organizer_id, type, name, info, price, start_time, location, rating, is_accepted) values
(1, 'MOVIE', 'Open Season', 'Movie about home-grown bear surviving in wild forrest', 300, TIMESTAMP '2023-05-15 11:50:00', 'Cinema Kinomir, str. Lenina, 101',0,true),
(1, 'THEATER', 'Crime and Punishment', 'Masterpiece of Fedor Dostoevsky on stage', 1500, TIMESTAMP '2023-05-26 18:00:00', 'Drama Theater, str Lenina, 4',0,true),
(1, 'CONCERT', 'Sabaton | Tomsk tour', 'Best rock hits from Sabaton!', 5000, TIMESTAMP '2023-05-23 20:00:00', 'Palace of Spectacles, str.Krasnoarmeyskaya, 124',0,true),
(1, 'EXPOSITION', 'Modern art for children', 'From children to children!', 250, TIMESTAMP '2023-05-28 12:00:00','Palace of Creativity for Children and Youth, str. Vershinina, 17',0,true),
(1, 'OTHER', 'BDK Club meeting', 'Only for BDK members',228,TIMESTAMP '2023-06-01 01:00:00', 'Secret Spot',0,true);


INSERT INTO event_comments(user_id,event_id,comment,creation_time) values
(1,5,'I am organizer, so I will be there! Do not tell anyone about secret spot!', TIMESTAMP '2023-05-19 18:02:00');