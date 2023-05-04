package ru.it.lab.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorizationService extends UserDetailsService {
    void login(String username, String password);

    void setAuthenticationManager(AuthenticationManager authenticationManager);

}
