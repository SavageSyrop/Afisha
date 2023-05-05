package ru.it.lab.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.it.lab.entitities.Authorization;

public interface AuthorizationService extends UserDetailsService {
    void login(Authorization authorization, String password);

    void setAuthenticationManager(AuthenticationManager authenticationManager);

}
