# Board API - Spring Boot 게시판 프로젝트

> Spring Boot를 활용한 RESTful API 기반 게시판 시스템

## 프로젝트 소개

Spring Boot 학습을 위해 개발한 게시판 API 프로젝트입니다.
사용자 관리, 게시글 CRUD, 댓글 시스템, 파일 업로드, 동적 검색 등 실무에서 필요한 주요 백엔드 기능을 구현했습니다.

### Highlights

| 영역 | 내용 |
|------|------|
| **테스트** | JUnit 5 + Mockito 단위/통합 테스트, K6 부하 테스트 (100 VUs, P95 18.7ms) |
| **CI/CD** | GitHub Actions + Docker + Codecov (커버리지 60%+) |
| **배포** | AWS EC2 Blue-Green 무중단 배포 (Nginx 트래픽 전환) |
| **성능 최적화** | N+1 문제 해결 (쿼리 21회 → 1회), 카테고리 캐싱 적용 |

### 주요 기능

- **사용자 관리**: 회원가입, 로그인, 프로필 관리
- **게시글 시스템**: CRUD, 조회수, 카테고리별 분류
- **댓글 시스템**: 계층형 댓글(대댓글), Soft Delete
- **파일 업로드**: 프로필 이미지, 게시글 다중 이미지(최대 10개)
- **동적 검색**: QueryDSL을 활용한 다중 조건 검색
- **페이징 & 정렬**: 효율적인 데이터 조회
- **예외 처리**: 통합 예외 처리 및 에러 응답 표준화
- **API 문서화**: Swagger/OpenAPI 3.0

---

## 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Data JPA** - ORM 및 데이터 접근
- **QueryDSL** - 동적 쿼리
- **Spring Security** - JWT 인증/인가
- **Validation** - 입력값 검증

### Database
- **MySQL 8.0** - 운영 DB
- **H2** - 테스트 DB (In-Memory)

### DevOps & Tools
- **Docker & Docker Compose** - 컨테이너화
- **GitHub Actions** - CI/CD
- **AWS EC2** - 배포 환경
- **Nginx** - 리버스 프록시 및 Blue-Green 배포
- **Codecov** - 테스트 커버리지
- **K6** - 부하 테스트

### Test
- **JUnit 5**
- **Mockito**
- **MockMvc** - 통합 테스트

---

## 프로젝트 구조

```
src/main/java/com/cho/board/
├── auth/               # JWT 인증/인가
├── category/           # 카테고리 (캐싱 적용)
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── comment/            # 계층형 댓글
├── post/               # 게시글 CRUD
├── user/               # 사용자 관리
├── file/               # 파일 업로드
└── global/             # 공통 기능
    ├── common/         # BaseEntity
    ├── config/         # Security, Swagger, QueryDSL
    ├── constants/      # 상수 관리
    ├── exception/      # 통합 예외 처리
    ├── response/       # API 응답 표준화
    ├── util/           # 유틸리티
    └── validation/     # 커스텀 Validator
```

---

## 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Docker (선택사항)

### 로컬 실행

```bash
# 1. 프로젝트 클론
git clone https://github.com/0802222/Board_R.git
cd Board_R

# 2. 환경 변수 설정 (.env 파일 생성)
JDBC_DATABASE_URL=jdbc:mysql://localhost:3306/board_db?useSSL=false&serverTimezone=Asia/Seoul
JDBC_DATABASE_USERNAME=your_username
JDBC_DATABASE_PASSWORD=your_password

# 3. 애플리케이션 실행
./gradlew bootRun
```

### Docker Compose로 실행

```bash
docker-compose up -d
```

### API 문서 확인

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

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

PR 생성 시 자동 실행:
- 테스트 실행 + JaCoCo 커버리지 측정
- Codecov 리포트 업로드
- PR 코멘트로 커버리지 변화 표시

---

## 성능 테스트 (K6)

100명 동시 접속 환경에서 부하 테스트 수행

### 테스트 결과

| API | VUs | 총 요청 | 성공률 | P95 | Peak RPS |
|-----|-----|--------|--------|-----|----------|
| 게시글 조회 | 100 | 6,723 | 100% | 18.7ms | 55.67 |
| 회원가입 (Stress) | 100 | 18,100 | 100% | 721ms | 63.67 |
| 회원가입 (Spike) | 100 | 4,300 | 100% | 909ms | 62.33 |

### 주요 성과

- 전체 27,000+ 요청 처리, 실패 0건
- 게시글 조회 API: 평균 8.5ms 응답
- 트래픽 급증(Spike) 시나리오 대응 성공

상세 결과: [K6_TEST_RESULTS.md](./docs/performance/K6_TEST_RESULTS.md)

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

회원가입 시 이메일 전송으로 인한 응답 지연 해결

| 구분 | 응답 시간 |
|------|----------|
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
