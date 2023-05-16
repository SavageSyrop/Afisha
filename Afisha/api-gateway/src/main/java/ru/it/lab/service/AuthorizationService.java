package ru.it.lab.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.it.lab.entities.Authorization;

public interface AuthorizationService extends UserDetailsService {
    String login(Authorization authorization, String username, String password);

    void setAuthenticationManager(AuthenticationManager authenticationManager);

}
