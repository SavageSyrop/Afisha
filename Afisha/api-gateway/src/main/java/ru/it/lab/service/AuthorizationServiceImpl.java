package ru.it.lab.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.it.lab.configuration.SecurityConstants;
import ru.it.lab.entities.Authorization;
import ru.it.lab.exceptions.AuthorizationErrorException;

import java.util.Date;

@Component
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private BCryptPasswordEncoder encoder;

    private AuthenticationManager authenticationManager;

    private Authorization temp = new Authorization();

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String login(Authorization authorization, String username, String password) {
        temp = authorization;
        if (encoder.matches(password, authorization.getPassword()) && authorization.getUsername().equals(username)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authorization, password, authorization.getAuthorities());
            try {
                authenticationManager.authenticate(usernamePasswordAuthenticationToken);
                if (usernamePasswordAuthenticationToken.isAuthenticated()) {
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    String token = JWT.create()
                            .withSubject(authorization.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                            .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));
                    return token;
                }
            } catch (Exception e) {
                throw new AuthorizationErrorException(e.getMessage());
            }
        }
        temp = null;
        throw new AuthorizationErrorException("Error during login");
    }

    public BCryptPasswordEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return temp;
    }
}
