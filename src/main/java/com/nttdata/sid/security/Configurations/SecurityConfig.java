package com.nttdata.sid.security.Configurations;

import com.nttdata.sid.repositories.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import com.nttdata.sid.security.entities.AppUser;
import com.nttdata.sid.security.filters.JwtAuthenticationFilter;
import com.nttdata.sid.security.filters.JwtAuthorizationFilter;
import com.nttdata.sid.security.services.ISecurityService;
import com.nttdata.sid.security.services.SecurityServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@CrossOrigin("*")
@Slf4j
public class SecurityConfig {
    private ISecurityService securityService;
    private UtilisateurRepository utilisateurRepository;


    public SecurityConfig( ISecurityService securityService,UtilisateurRepository utilisateurRepository) {

        this.securityService = securityService;
        this.utilisateurRepository=utilisateurRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(Customizer.withDefaults());
        httpSecurity.csrf(csrf->csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
        httpSecurity.csrf(csrf->csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/login")));
        httpSecurity.csrf(csrf->csrf.disable());

        //  httpSecurity.headers(headers -> headers.frameOptions().disable());
        httpSecurity.sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll());
        httpSecurity.authorizeHttpRequests(auth->auth.requestMatchers(
                        "/forgot-password/**", "/reset-password/**","/refreshToken/**"
                        ,"/swagger-ui/**","/v3/**","/v1/**","/login/**","/signin",
                        "/formUserRole").permitAll()

                .anyRequest().authenticated()
        );
        httpSecurity.addFilter(new JwtAuthenticationFilter(authenticationManager(authenticationConfiguration()), securityService,utilisateurRepository));
        httpSecurity.addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                AppUser appUser = securityService.findUserByEmail(email);
                log.info("user = "+appUser);
                log.info("roles "+appUser.getUserRoles());
                return new User(
                        appUser.getEmail(),
                        appUser.getPassword(),
                        appUser.getUserRoles()
                                .stream()
                                .map(gr -> new SimpleGrantedAuthority(gr.getRoleName()))
                                .collect(Collectors.toList())
                );
            }
        };
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Primary
    AuthenticationConfiguration authenticationConfiguration(){
        return new AuthenticationConfiguration();
    }



}
