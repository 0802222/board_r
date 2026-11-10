# Board API

Spring Boot 3 / Java 21 기반 게시판 REST API입니다. 회원(JWT 인증, 이메일 인증), 게시글 CRUD, 계층형 댓글(Soft Delete), 이미지 업로드, QueryDSL 동적 검색을 제공합니다. JPA N+1 문제를 Fetch Join으로 해결(게시글 목록 쿼리 21회 -> 1회)했고, GitHub Actions + Docker + Nginx Blue-Green 방식의 배포 파이프라인과 K6 부하 테스트까지 학습 목적으로 직접 구성했습니다.

## 기술 스택

| 구분 | 사용 기술 |
|------|-----------|
| Language / Framework | Java 21, Spring Boot 3.5.5, Spring Security (JWT), Spring Data JPA, Validation |
| 쿼리 | QueryDSL 5, Spring Cache |
| DB | MySQL 8.0 (운영/개발), H2 (테스트) |
| 테스트 | JUnit 5, Mockito, MockMvc, JaCoCo, K6 |
| 문서화 | springdoc-openapi (Swagger UI) |
| 인프라 / CI/CD | Docker, Docker Compose, GitHub Actions, GHCR, AWS EC2, Nginx |

테스트 코드는 `@Test` 약 78개입니다.

## 빠른 시작

```bash
# 1. 클론
git clone https://github.com/0802222/board_r.git
cd board_r

# 2. 환경 변수 (.env 파일 또는 OS 환경 변수, 값은 직접 설정)
#    JDBC_DATABASE_URL, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD
#    JWT_SECRET, JWT_ACCESS_EXPIRATION, JWT_REFRESH_EXPIRATION
#    MAIL_USERNAME, MAIL_PASSWORD
#    SPRING_PROFILES_ACTIVE (선택, 기본값 local)

# 3. 실행
./gradlew bootRun

# 4. 테스트 (JaCoCo 리포트: build/reports/jacoco/test/index.html)
./gradlew test jacocoTestReport
```

- Docker Compose: `docker compose up -d` (같은 `.env` 사용, 이미지는 GHCR에서 pull)
- Swagger UI: http://localhost:8080/swagger-ui.html

## 아키텍처 개요

```
Client -> Nginx -> Spring Boot (Controller -> Service -> Repository) -> MySQL
                       |-- Spring Security + JWT 필터 (인증/인가)
                       |-- Spring Cache (카테고리 목록)
                       `-- 로컬 파일 저장소 (이미지 업로드)
```

도메인별 패키지(`auth`, `category`, `comment`, `post`, `user`, `file`)와 공통 모듈(`global`: config, exception, response, util, validation)로 구성했습니다.

```
src/main/java/com/cho/board/
├── auth/  category/  comment/  post/  user/  file/
└── global/   # common, config, constants, exception, response, util, validation
```

## 한계와 다음 개선

- 부하 테스트는 로컬(MacBook Air M2) 단일 환경, 최대 100 VUs 기준이며 운영 환경 수치가 아닙니다. 자세한 조건은 [성능 테스트](#성능-테스트-k6) 참고.
- 테스트 커버리지 수치는 아직 README에 기재하지 않았습니다. CI(JaCoCo/Codecov) 측정값 확인 후 임계값 상향 예정입니다.
- 인덱스/실행 계획(EXPLAIN) 기반 쿼리 튜닝은 아직 하지 않았습니다.
- 배포 대상은 단일 EC2 인스턴스이며 모니터링/알림은 구성하지 않았습니다.
- 파일은 서버 로컬 디스크에 저장합니다 (다중 인스턴스 시 S3 같은 외부 저장소 필요).

---

## API 명세

### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/auth/email/send-verification` | 이메일 인증코드 전송 |
| POST | `/auth/email/verify` | 이메일 인증코드 확인 |
| POST | `/auth/signup` | 회원가입 |
| POST | `/auth/login` | 로그인 (JWT 발급) |
| POST | `/auth/refresh` | 토큰 갱신 |

### 게시글 (Posts)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/posts` | 게시글 작성 |
| GET | `/posts` | 게시글 목록 조회 (페이징) |
| GET | `/posts/{id}` | 게시글 상세 조회 |
| PUT | `/posts/{id}` | 게시글 수정 |
| DELETE | `/posts/{id}` | 게시글 삭제 |
| GET | `/posts/search` | 게시글 검색 (QueryDSL) |

### 댓글 (Comments)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/posts/{postId}/comments` | 댓글 작성 |
| GET | `/posts/{postId}/comments` | 댓글 목록 조회 |
| GET | `/posts/{postId}/comments/{commentId}` | 댓글 상세 조회 |
| PUT | `/posts/{postId}/comments/{commentId}` | 댓글 수정 |
| DELETE | `/posts/{postId}/comments/{commentId}` | 댓글 삭제 (Soft Delete) |

### 사용자 (Users)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/users/me` | 내 프로필 조회 |
| PUT | `/users/me` | 내 프로필 수정 |
| PUT | `/users/me/profile-image` | 프로필 이미지 변경 |
| PUT | `/users/me/password` | 비밀번호 변경 |
| DELETE | `/users/me` | 회원 탈퇴 |
| GET | `/users` | 전체 사용자 조회 (Admin) |

### 카테고리 (Categories)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/categories` | 카테고리 목록 조회 |
| GET | `/categories/{id}` | 카테고리 상세 조회 |
| POST | `/categories` | 카테고리 생성 |

### 파일 (Files)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/files/temp` | 임시 파일 업로드 |
| POST | `/files/posts/{postId}/images` | 게시글 이미지 업로드 |
| GET | `/files/posts/{postId}/images` | 게시글 이미지 목록 |
| DELETE | `/files/posts/{postId}/images/{imageId}` | 이미지 삭제 |
| GET | `/files/images/{filename}` | 이미지 파일 조회 |

자세한 API 명세는 Swagger UI를 참고하세요.

---

## 데이터베이스 설계

```
Users
├── id (PK)
├── email (UNIQUE)
├── password (BCrypt)
├── nickname
├── role
├── created_at, updated_at

Posts
├── id (PK)
├── title, content
├── view_count
├── author_id (FK → Users)
├── category_id (FK → Categories)
├── created_at, updated_at

Comments
├── id (PK)
├── content
├── deleted (Soft Delete)
├── post_id (FK → Posts)
├── author_id (FK → Users)
├── parent_id (FK → Comments, 대댓글)

Categories
├── id (PK)
├── category_type (ENUM)
├── is_active

Post_Images
├── id (PK)
├── file_path, original_filename
├── file_size, display_order
├── post_id (FK → Posts)
```

---

## 테스트 전략

### 테스트 구조

```
src/test/java/
├── service/                 # 단위 테스트 (Mockito)
│   ├── PostServiceTest
│   ├── UserServiceTest
│   └── CategoryServiceTest
├── controller/              # API 테스트 (MockMvc)
│   ├── PostControllerTest
│   └── AuthControllerTest
├── integration/             # 통합 테스트
│   ├── PostIntegrationTest
│   ├── UserIntegrationTest
│   └── SecurityIntegrationTest
└── fixture/                 # 테스트 데이터
    ├── UserFixture
    ├── PostFixture
    └── CategoryFixture
```

### 테스트 실행

```bash
./gradlew test                          # 전체 테스트
./gradlew test jacocoTestReport         # 커버리지 리포트
```

### CI 파이프라인

- PR(main, develop) 및 push(main, develop) 시 테스트 + JaCoCo 리포트 + 커버리지 검증(`jacocoTestCoverageVerification`) 실행
- Codecov 리포트 업로드, PR 코멘트로 커버리지 표시
- develop push 시 스테이징 배포는 테스트 통과 후에만 진행

---

## 성능 테스트 (K6)

### 테스트 환경과 한계

- 환경: 로컬 개발 환경 (MacBook Air 15, Apple M2), local 프로필, MySQL 8.0, Java 21
- 도구: Grafana Cloud K6 무료 플랜 (최대 100 VUs)
- 앱, DB, 부하 생성기가 같은 환경이라 서로 자원을 경쟁할 수 있고 네트워크 지연이 반영되지 않습니다.
- 엔드포인트별 단일 시나리오이며 로그인/댓글 등을 섞은 사용자 시나리오는 아직 없습니다.
- 따라서 아래 수치는 운영 성능이 아니라 "개선 전후를 같은 조건에서 비교하기 위한 기준값"입니다.

### 결과

| API | VUs | 총 요청 | 성공률 | P95 | Peak RPS |
|-----|-----|--------|--------|-----|----------|
| 게시글 조회 | 100 | 6,723 | 100% | 18.7ms | 55.67 |
| 회원가입 (Stress) | 100 | 18,100 | 100% | 721ms | 63.67 |
| 회원가입 (Spike) | 100 | 4,300 | 100% | 909ms | 62.33 |

시나리오 설정, 그래프, 종합 분석: [docs/performance/K6_TEST_RESULTS.md](./docs/performance/K6_TEST_RESULTS.md)

### TODO: 쿼리 실행 계획(EXPLAIN) 분석

아직 수행하지 않았습니다. 수행 후 이 섹션에 기록할 예정입니다 (수치는 측정 전까지 기재하지 않음).

- [ ] 게시글 목록/검색 쿼리에 `EXPLAIN` 실행 (type, key, rows 확인)
- [ ] 인덱스 추가 전후 비교 (같은 데이터 건수, 같은 조건)
- [ ] 결과를 `docs/performance/` 에 정리하고 링크

---

## 기술적 문제 해결

### 1. N+1 문제 해결

**문제**: 게시글 10개 조회 시 쿼리 21회 실행

```sql
SELECT * FROM posts                    -- 1회
SELECT * FROM users WHERE id = ?       -- 10회 (작성자)
SELECT * FROM categories WHERE id = ?  -- 10회 (카테고리)
```

**해결**: Fetch Join 적용

```java
@Query("SELECT p FROM Post p " +
        "JOIN FETCH p.author " +
        "JOIN FETCH p.category")
List<Post> findAllWithDetails();
```

**결과**: 쿼리 1회로 감소 (95% 개선)

### 2. 카테고리 캐싱

자주 조회되는 카테고리 목록에 Spring Cache 적용

```java
@Cacheable(value = "categories", key = "'active'")
public List<CategoryResponse> getActiveCategories() {
  return categoryRepository.findByIsActiveTrue();
}
```

### 3. 비동기 이메일 전송

회원가입 시 이메일 전송으로 인한 응답 지연을 비동기 처리로 해결했습니다. 측정 조건(단건 측정 vs K6 부하 평균)이 달라 전후 수치의 직접 비교는 [K6_TEST_RESULTS.md](./docs/performance/K6_TEST_RESULTS.md)의 조건을 확인한 뒤 해석해야 합니다.

------|----------|
| Before (동기) | 417ms |
| After (비동기) | 134ms |

**67% 개선**

---

## 배포 아키텍처

### Blue-Green 무중단 배포

```
GitHub (main branch)
    │
    ▼
GitHub Actions
    │ Docker Build & Push (GHCR)
    ▼
AWS EC2
    │
    ├── [Blue Container :8080]
    │         ▲
    │         │ (트래픽 전환)
    ├── Nginx ─┤
    │         │
    │         ▼
    └── [Green Container :8081]
```

### 배포 흐름

1. main 브랜치 푸시 → GitHub Actions 트리거
2. Docker 이미지 빌드 및 GHCR 푸시
3. 신규 컨테이너 배포 (Blue 또는 Green)
4. Health Check 통과 확인 (최대 120초)
5. Nginx 설정 변경으로 트래픽 전환
6. 배포 후 검증 (5회 헬스체크)
7. 이전 컨테이너 종료

### 롤백 전략

- Health Check 실패 시 자동 롤백
- Nginx 설정 복구 → 이전 컨테이너로 트래픽 복원
- 배포 후 검증 실패 시에도 자동 롤백

---

## 회고

### 구현 성과
- 체계적인 도메인 구조와 레이어 분리
- N+1 문제 해결을 통한 성능 최적화 (95% 쿼리 감소)
- 테스트 코드 작성 (단위 + 통합 + 성능)
- CI/CD 파이프라인 구축 (테스트 자동화 + 커버리지)
- Blue-Green 무중단 배포 구현
- K6를 활용한 부하 테스트 및 성능 검증

### 배운 점
- JPA N+1 문제의 원인과 해결 방법 (Fetch Join, @EntityGraph, BatchSize)
- QueryDSL을 활용한 동적 쿼리 작성
- Docker와 GitHub Actions를 활용한 자동화 배포
- K6를 활용한 성능 테스트 시나리오 설계 (Load, Stress, Spike)

---

## 문의

- **Blog**: https://devmee.tistory.com/
- **GitHub**: https://github.com/0802222
