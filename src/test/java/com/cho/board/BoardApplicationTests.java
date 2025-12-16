package com.cho.board;

import com.cho.board.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestSecurityConfig.class)
class BoardApplicationTests {

    @Test
    void contextLoads() {
    }

}
