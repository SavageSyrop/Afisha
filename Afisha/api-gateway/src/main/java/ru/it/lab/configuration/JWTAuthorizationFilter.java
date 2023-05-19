package ru.it.lab.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.it.lab.Role;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.entities.Authorization;
import ru.it.lab.entities.Permission;
import ru.it.lab.enums.PermissionType;
import ru.it.lab.enums.RoleType;
import ru.it.lab.exceptions.AuthorizationErrorException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserServiceGrpc.UserServiceBlockingStub userService;

    private final AccessDeniedHandler accessDeniedHandler;

    public JWTAuthorizationFilter(AuthenticationManager authManager, AccessDeniedHandler accessDeniedHandler, UserServiceGrpc.UserServiceBlockingStub userService) {
        super(authManager);
        this.accessDeniedHandler = accessDeniedHandler;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String cookieAuthValue = null;
        Cookie[] cookies = req.getCookies();
        Cookie found = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SecurityConstants.AUTHORIZATION_COOKIE)) {
                    cookieAuthValue = cookie.getValue();
                    found = cookie;
                }
            }
        }


        if (cookieAuthValue == null) {
            try {
                chain.doFilter(req, res);
            } catch (IOException | ServletException exception) {
                log.error(exception.getMessage());
                throw new JWTVerificationException("No token");
            }
            return;
        }

        Authorization authorization = getAuthorization(cookieAuthValue);
        if (authorization == null) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("JWT token stores invalid data! Please login again and receive new token!");
            found.setMaxAge(0);
            res.addCookie(found);
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authorization.getUsername(), authorization.getPassword(), authorization.getAuthorities());


        if (authorization.getIsBanned()) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("You are banned!");
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        if (!authorization.getActivationCode().equals("")) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("Please activate your account! We have send a letter at your email!");
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            chain.doFilter(req, res);
        } catch (IOException | ServletException exception) {
            log.error(exception.getMessage());
            throw new AuthorizationErrorException(exception.getMessage());
        }
    }

    private Authorization getAuthorization(String token) {
        try {
            String user = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()))
                    .build()
                    .verify(token)
                    .getSubject();
            if (user != null) {
                return getUserData(user);
            }
        } catch (TokenExpiredException tokenExpiredException) {
            return null;
        }
        return null;
    }

    private Authorization getUserData(String username) {
        UserProto userProto = userService.getLoginData(UserProto.newBuilder().setUsername(username).build());
        Authorization authorization = new Authorization();
        authorization.setUsername(userProto.getUsername());
        authorization.setPassword(userProto.getPassword());
        Role role = userProto.getRole();
        List<Permission> permissionList = new ArrayList<>();
        for (ru.it.lab.Permission permission : role.getPermissionList()) {
            permissionList.add(new Permission(PermissionType.valueOf(permission.getName())));
        }
        ru.it.lab.entities.Role authRole = new ru.it.lab.entities.Role();
        authRole.setName(RoleType.valueOf(role.getName()));
        authRole.setPermissions(permissionList);
        authorization.setRole(authRole);
        authorization.setIsBanned(userProto.getIsBanned());
        authorization.setActivationCode(userProto.getActivationCode());
        return authorization;
    }
}
