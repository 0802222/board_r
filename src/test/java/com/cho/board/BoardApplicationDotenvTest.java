package com.cho.board;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cdimascio.dotenv.DotenvEntry;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// .env 값을 시스템 프로퍼티로 등록하는 기능을 확인하는 테스트
class BoardApplicationDotenvTest {

    // 테스트에서만 쓰는 가짜 키 이름 (실제 설정과 겹치지 않게 한다)
    private static final String TEST_PROPERTY_KEY = "TEST_DOTENV_PROPERTY_KEY";

    // 운영체제 환경변수로 항상 존재하는 키 (실제 환경변수가 우선인지 확인할 때 쓴다)
    private static final String EXISTING_ENVIRONMENT_KEY = "PATH";

    // 각 테스트가 끝나면 등록한 시스템 프로퍼티를 지워서 다른 테스트에 영향이 없게 한다
    @AfterEach
    void clearTestProperties() {
        // 테스트용 키를 지운다
        System.clearProperty(TEST_PROPERTY_KEY);

        // 운영체제 환경변수와 같은 이름의 프로퍼티도 지운다
        System.clearProperty(EXISTING_ENVIRONMENT_KEY);
    }

    @Test
    @DisplayName(".env 항목이 시스템 프로퍼티로 등록된다")
    void registersDotenvEntryAsSystemProperty() {
        // .env 파일에서 읽은 것처럼 항목 하나를 만든다
        List<DotenvEntry> dotenvEntries = List.of(new DotenvEntry(TEST_PROPERTY_KEY, "from-dotenv"));

        // 항목을 시스템 프로퍼티로 등록한다
        BoardApplication.registerDotenvEntriesAsSystemProperties(dotenvEntries);

        // 같은 값으로 읽히는지 확인한다
        assertThat(System.getProperty(TEST_PROPERTY_KEY)).isEqualTo("from-dotenv");
    }

    @Test
    @DisplayName("이미 설정된 시스템 프로퍼티는 .env 값으로 덮어쓰지 않는다")
    void doesNotOverwriteExistingSystemProperty() {
        // 먼저 같은 이름의 시스템 프로퍼티를 직접 설정해 둔다
        System.setProperty(TEST_PROPERTY_KEY, "already-set");

        // .env에 같은 이름의 다른 값이 있는 상황을 만든다
        List<DotenvEntry> dotenvEntries = List.of(new DotenvEntry(TEST_PROPERTY_KEY, "from-dotenv"));

        // 항목을 시스템 프로퍼티로 등록한다
        BoardApplication.registerDotenvEntriesAsSystemProperties(dotenvEntries);

        // 기존 값이 그대로인지 확인한다
        assertThat(System.getProperty(TEST_PROPERTY_KEY)).isEqualTo("already-set");
    }

    @Test
    @DisplayName("운영체제 환경변수가 이미 있으면 .env 값으로 덮어쓰지 않는다")
    void doesNotOverrideExistingEnvironmentVariable() {
        // 운영체제 환경변수와 같은 이름의 항목이 .env에 있는 상황을 만든다
        List<DotenvEntry> dotenvEntries = List.of(new DotenvEntry(EXISTING_ENVIRONMENT_KEY, "from-dotenv"));

        // 항목을 시스템 프로퍼티로 등록한다
        BoardApplication.registerDotenvEntriesAsSystemProperties(dotenvEntries);

        // 시스템 프로퍼티가 만들어지지 않아야 실제 환경변수 값이 쓰인다
        assertThat(System.getProperty(EXISTING_ENVIRONMENT_KEY)).isNull();
    }
}
