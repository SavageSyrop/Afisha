package ru.it.lab.dao;


import ru.it.lab.entities.User;

public interface UserDao extends AbstractDao<User> {
    User getByUsername(String username);

    User getByActivationCode(String activationCode);

    User getByResetCode(String code);
}
