package com.nttdata.sid.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.repositories.UtilisateurRepository;
import com.nttdata.sid.security.entities.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.nttdata.sid.security.Util.JwtUtil;
import com.nttdata.sid.security.repositories.AppUserRepository;
import com.nttdata.sid.security.services.ISecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private AppUserRepository appUserRepository;

    private ISecurityService securityService;


    private UtilisateurRepository utilisateurRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, ISecurityService securityService, UtilisateurRepository utilisateurRepository) {
        this.authenticationManager = authenticationManager;
        this.securityService = securityService;
        this.utilisateurRepository=utilisateurRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("attemptAuthentication");
        String email, password;
        try {
            email = request.getParameter("email");
            password = request.getParameter("password");
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            return authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication");

        User user = (User) authResult.getPrincipal(); // qui permet de retourner l'utilisateur authentifé
        AppUser appUser = securityService.findUserByEmail(user.getUsername());
        Algorithm algorithm = Algorithm.HMAC256(JwtUtil.SECRET);
        String jwtAccessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis()+ JwtUtil.EXPIRE_ACCESS_TOKEN))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles",user.getAuthorities().stream().map(gr->gr.getAuthority()).collect(Collectors.toList()))
                .sign(algorithm);
        System.out.println("roles "+user.getAuthorities().stream().map(gr->gr.getAuthority()).collect(Collectors.toList()));
        String jwtRefreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis()+ JwtUtil.EXPIRE_REFRESH_TOKEN))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
        Map<String,String> idToken = new HashMap<>();
        idToken.put("access-token",jwtAccessToken);
        idToken.put("refresh-token",jwtRefreshToken);
        Optional<Utilisateur> u = utilisateurRepository.findUtilisateurByEmail(user.getUsername());
        idToken.put("id", String.valueOf(u.get().getId()));
        response.setContentType("application/json");
        response.setHeader(JwtUtil.AUTH_HEADER,jwtAccessToken);
        new ObjectMapper().writeValue(response.getOutputStream(),idToken);
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.error("Authentication failed for user: {}", failed.getMessage());
        response.setStatus(401);
    }
}
