package ru.it.lab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.it.lab.dao.UserDao;
import ru.it.lab.exceptions.AuthorizationErrorException;

import javax.persistence.EntityNotFoundException;

@Component
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService{
    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder encoder;

    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.getByUsername(username);
    }

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void login(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        if (userDetails == null) {
            throw new EntityNotFoundException("User with username " + username + " doesn't exist");
        }
        if (encoder.matches(password, userDetails.getPassword())) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            if (usernamePasswordAuthenticationToken.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                return;
            }
        }
        throw new AuthorizationErrorException("Error during login");
    }
}
