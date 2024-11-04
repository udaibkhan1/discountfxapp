package com.discountcalc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Profile("!test")
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService(SecurityProperties securityProperties) {
        UserDetails user = User.builder()
                .username(securityProperties.getUser().getName())
                .password(passwordEncoder().encode(securityProperties.getUser().getPassword())) //NOSONAR
                .roles(String.join(",", securityProperties.getUser().getRoles()))
                .build();
        return new InMemoryUserDetailsManager(user);
    }
    @Bean
    DefaultSecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)//NOSONAR
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers("/api/login", "/site/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

}