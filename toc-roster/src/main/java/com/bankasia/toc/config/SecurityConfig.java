package com.bankasia.toc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                        .requestMatchers("/css/**", "/login", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/roster", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** ADMIN edits rosters and staff; VIEWER is read-only. */
    @Bean
    UserDetailsService users(PasswordEncoder pe,
                             @Value("${app.security.admin-user}") String au,
                             @Value("${app.security.admin-password}") String ap,
                             @Value("${app.security.viewer-user}") String vu,
                             @Value("${app.security.viewer-password}") String vp) {
        return new InMemoryUserDetailsManager(
                User.withUsername(au).password(pe.encode(ap)).roles("ADMIN", "VIEWER").build(),
                User.withUsername(vu).password(pe.encode(vp)).roles("VIEWER").build());
    }
}
