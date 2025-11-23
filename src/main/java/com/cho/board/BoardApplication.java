package com.cho.board;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.cho.board")
@EnableJpaRepositories("com.cho.board")
public class BoardApplication {

    public static void main(String[] args) {

        try {
            // env load
            Dotenv dotenv = Dotenv
                .configure()
                .ignoreIfMissing()
                .load();

            if (dotenv.get("JDBC_DATABASE_URL") != null) {
                System.setProperty("spring.datasource.url", dotenv.get("JDBC_DATABASE_URL"));
                System.setProperty("spring.datasource.username",
                    dotenv.get("JDBC_DATABASE_USERNAME"));
                System.setProperty("spring.datasource.password",
                    dotenv.get("JDBC_DATABASE_PASSWORD"));
            }
        } catch (Exception e) {

        }
        SpringApplication.run(BoardApplication.class, args);
    }
}
