package com.cho.board;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.cho.board")
@EnableJpaRepositories("com.cho.board")
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@EnableScheduling
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

            // .env의 나머지 항목(JWT, 메일 등)도 시스템 프로퍼티로 등록
            registerDotenvEntriesAsSystemProperties(dotenv.entries());
        } catch (Exception e) {
            System.out.println("Using default configuration (no .env file)");
        }
        SpringApplication.run(BoardApplication.class, args);
    }

    // .env 항목을 시스템 프로퍼티로 등록한다 (이미 설정된 값은 덮어쓰지 않는다)
    static void registerDotenvEntriesAsSystemProperties(Iterable<DotenvEntry> dotenvEntries) {
        // .env 항목을 하나씩 확인
        for (DotenvEntry dotenvEntry : dotenvEntries) {
            // 시스템 프로퍼티나 운영체제 환경변수에 같은 이름이 있는지 확인
            boolean isAlreadyConfigured = System.getProperty(dotenvEntry.getKey()) != null
                || System.getenv(dotenvEntry.getKey()) != null;

            // 아직 없는 값만 등록 (Docker처럼 실제 환경변수가 있으면 그 값을 우선)
            if (!isAlreadyConfigured) {
                System.setProperty(dotenvEntry.getKey(), dotenvEntry.getValue());
            }
        }
    }
}
