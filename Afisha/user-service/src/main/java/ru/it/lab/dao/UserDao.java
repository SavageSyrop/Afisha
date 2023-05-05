package ru.it.lab.dao;


import ru.it.lab.entitities.User;

public interface UserDao extends AbstractDao<User>{
    User getByUsername(String username);
}
