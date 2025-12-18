package com.cho.board;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.cho.board")
@EnableJpaRepositories("com.cho.board")
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class BoardApplication {

    public static void main(String[] args) {

        try {
            // env load
            Dotenv dotenv = Dotenv
                .configure()
                .ignoreIfMissing()
                .load();

            // 프로파일에 따라 DB 설정 선택
            String profile = System.getProperty("spring.profiles.active", "local");

            if (profile.equals("local") && dotenv.get("JDBC_DATABASE_URL_LOCAL") != null) {
                // 로컬 개발 환경
                System.setProperty("spring.datasource.url", dotenv.get("JDBC_DATABASE_URL_LOCAL"));
                System.setProperty("spring.datasource.username",
                    dotenv.get("JDBC_DATABASE_USERNAME_LOCAL"));
                System.setProperty("spring.datasource.password",
                    dotenv.get("JDBC_DATABASE_PASSWORD_LOCAL"));
            } else if (dotenv.get("JDBC_DATABASE_URL") != null) {
                // Docker 환경
                System.setProperty("spring.datasource.url", dotenv.get("JDBC_DATABASE_URL"));
                System.setProperty("spring.datasource.username",
                    dotenv.get("JDBC_DATABASE_USERNAME"));
                System.setProperty("spring.datasource.password",
                    dotenv.get("JDBC_DATABASE_PASSWORD"));
            }

            // JWT 환경변수 설정
            if (dotenv.get("JWT_SECRET") != null) {
                System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
                System.setProperty("JWT_ACCESS_EXPIRATION", dotenv.get("JWT_ACCESS_EXPIRATION"));
                System.setProperty("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));
            }
        } catch (Exception e) {

        }
        SpringApplication.run(BoardApplication.class, args);
    }
}
