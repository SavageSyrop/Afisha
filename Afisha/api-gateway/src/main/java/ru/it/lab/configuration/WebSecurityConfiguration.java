package ru.it.lab.configuration;


import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.exceptions.handlers.CustomAccessDeniedHandler;
import ru.it.lab.exceptions.handlers.DelegatedAuthenticationEntryPoint;
import ru.it.lab.service.AuthorizationService;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    private DelegatedAuthenticationEntryPoint authenticationEntryPoint;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        try {
            authorizationService.setAuthenticationManager(customAuthenticationManager());
            http.csrf().disable()
                    .cors().and().authorizeRequests()
                    .antMatchers("/perform_logout","/sign_up", "/forgot_password", "/reset_password/*", "/activate/*","/events/all", "/events/type").permitAll()
                    .antMatchers(HttpMethod.POST, "/login").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .logout()
                    .logoutUrl("/perform_logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("Authorization")
                    .permitAll()
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler())
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .and()
                    .addFilter(new JWTAuthorizationFilter(authenticationManager(), accessDeniedHandler,userService))
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//            http.csrf().disable();
//            http.authorizeRequests()
//                    .antMatchers("/perform_logout","/sign_up", "/login", "/forgot_password", "/reset_password/*", "/activate/*", "/search").permitAll()
//                    .anyRequest().authenticated()
//                    .and()
////                    .rememberMe()
////                    .tokenValiditySeconds(86400)
////                    .rememberMeCookieName("rememberme")
////                    .alwaysRemember(true)
////                    .userDetailsService(authorizationService)
////                    .and()
//                    .httpBasic()
//                    .and()
//                    .logout()
//                    .logoutUrl("/perform_logout")
//                    .invalidateHttpSession(true)
//                    .deleteCookies("JSESSIONID")
//                    .permitAll()
//                    .and()
//                    .exceptionHandling()
//                    .accessDeniedHandler(accessDeniedHandler())
//                    .authenticationEntryPoint(authenticationEntryPoint)
//                    .and()
//                    .addFilter(new JWTAuthorizationFilter(authenticationManager(), accessDeniedHandler))
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new AuthorizationServiceException(exception.getMessage());
        }
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        try {
            auth.userDetailsService(authorizationService).passwordEncoder(encoder);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new SecurityException(exception.getMessage());
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
}