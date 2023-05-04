package ru.it.lab.configuration;


import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.it.lab.exceptions.handlers.CustomAccessDeniedHandler;
import ru.it.lab.exceptions.handlers.DelegatedAuthenticationEntryPoint;
import ru.it.lab.service.AuthorizationService;
import ru.it.lab.service.AuthorizationServiceImpl;

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

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    private DelegatedAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        try {
            authorizationService.setAuthenticationManager(customAuthenticationManager());
            http.csrf().disable()
                    .cors().and()
                    .authorizeRequests()
                    .antMatchers("/user/all","/sign_up", "/login", "/forgot_password", "/reset_password/*", "/activate/*", "/search","/", "/registration").permitAll()
                    .antMatchers(HttpMethod.GET,"/login", "/css/*", "/images/*").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .and()
                    .logout().permitAll()
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler())
                    .authenticationEntryPoint(authenticationEntryPoint);
//                    .and()
//                    .addFilter(new JWTAuthenticationFilter(authenticationManager(), accessDeniedHandler, userService))
//                    .addFilter(new JWTAuthorizationFilter(authenticationManager(), accessDeniedHandler, userService))
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