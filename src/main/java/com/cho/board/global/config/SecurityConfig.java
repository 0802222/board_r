package com.cho.board.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 테스트용 : CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/", "/error",
//                    "v3/api-docs/**",
//                    "/swagger-ui/**",
//                    "/swagger-ui.html",
//                    "/actuator/**",
//                    "/user",
//                    "/comment",
//                    "/category",
//                    "/post"
//                ).permitAll()
//                .anyRequest().authenticated()
                    .anyRequest().permitAll() // 테스트용 : 모든요청 허용
            );
//            .httpBasic(Customizer.withDefaults()); // 임시
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
