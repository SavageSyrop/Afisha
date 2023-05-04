package ru.it.lab.dao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.it.lab.entitities.User;

public interface UserDao extends AbstractDao<User>{
    UserDetails getByUsername(String username);
}
