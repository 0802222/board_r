# 📝 Board API - Spring Boot 게시판 프로젝트

> Spring Boot를 활용한 RESTful API 기반 게시판 시스템

## 📚 프로젝트 소개

Spring Boot 학습을 위해 개발한 게시판 API 프로젝트입니다.
사용자 관리, 게시글 CRUD, 댓글 시스템, 파일 업로드, 동적 검색 등 실무에서 필요한 주요 백엔드 기능을 구현했습니다.

### 주요 기능

- ✅ **사용자 관리**: 회원가입, 로그인, 프로필 관리
- ✅ **게시글 시스템**: CRUD, 조회수, 카테고리별 분류
- ✅ **댓글 시스템**: 계층형 댓글(대댓글), Soft Delete
- ✅ **파일 업로드**: 프로필 이미지, 게시글 다중 이미지(최대 10개)
- ✅ **동적 검색**: QueryDSL을 활용한 다중 조건 검색
- ✅ **페이징 & 정렬**: 효율적인 데이터 조회
- ✅ **예외 처리**: 통합 예외 처리 및 에러 응답 표준화
- ✅ **API 문서화**: Swagger/OpenAPI 3.0

---

## 🛠 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Data JPA** - ORM 및 데이터 접근
- **QueryDSL** - 동적 쿼리
- **Spring Security** - 보안 (BCrypt 암호화)
- **Validation** - 입력값 검증

### Database
- **MySQL 8.0** - 운영 DB
- **H2** - 테스트 DB (In-Memory)

### DevOps & Tools
- **Docker & Docker Compose** - 컨테이너화
- **GitHub Actions** - CI/CD
- **AWS EC2** - 배포 환경
- **Nginx** - 리버스 프록시 및 Blue-Green 배포
- **Gradle** - 빌드 도구

### Test
- **JUnit 5**
- **Mockito**
- **MockMvc** - 통합 테스트

### Documentation
- **SpringDoc OpenAPI** - API 문서 자동 생성

---

## 📂 프로젝트 구조

```
src/main/java/com/cho/board/
├── category/           # 카테고리 도메인
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── comment/            # 댓글 도메인
├── post/               # 게시글 도메인
├── user/               # 사용자 도메인
├── file/               # 파일 업로드
└── global/             # 공통 기능
    ├── common/         # BaseEntity
    ├── config/         # 설정 (Security, Swagger, QueryDSL)
    ├── constants/      # 상수 관리
    ├── exception/      # 예외 처리
    ├── response/       # API 응답 표준화
    ├── util/           # 유틸리티
    └── validation/     # 커스텀 Validator
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Docker (선택사항)

### 로컬 실행

#### 1. 프로젝트 클론

```bash
git clone https://github.com/0802222/Board_R.git
cd Board_R
```

#### 2. 환경 변수 설정

`.env` 파일 생성:

```properties
JDBC_DATABASE_URL=jdbc:mysql://localhost:3306/board_db?useSSL=false&serverTimezone=Asia/Seoul
JDBC_DATABASE_USERNAME=your_username
JDBC_DATABASE_PASSWORD=your_password
```

#### 3. 애플리케이션 실행

```bash
# Gradle로 빌드 및 실행
./gradlew bootRun

# 또는 JAR 파일 생성 후 실행
./gradlew clean bootJar
java -jar build/libs/board-0.0.1-SNAPSHOT.jar
```

#### 4. API 문서 확인

브라우저에서 접속:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Docker Compose로 실행

```bash
docker-compose up -d
```

---

## 📖 API 명세

### 주요 엔드포인트

#### 사용자 (Users)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/users` | 회원가입 |
| GET | `/users` | 전체 사용자 조회 |
| GET | `/users/{id}` | 특정 사용자 조회 |
| PUT | `/users/{id}` | 사용자 정보 수정 |
| DELETE | `/users/{id}` | 사용자 삭제 |
| POST | `/users/{id}/profile-image` | 프로필 이미지 업로드 |

#### 게시글 (Posts)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/posts` | 게시글 작성 |
| GET | `/posts` | 게시글 목록 조회 (페이징) |
| GET | `/posts/{id}` | 게시글 상세 조회 |
| PUT | `/posts/{id}` | 게시글 수정 |
| DELETE | `/posts/{id}` | 게시글 삭제 |
| GET | `/posts/search` | 게시글 검색 (동적 쿼리) |

#### 댓글 (Comments)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/posts/{postId}/comments` | 댓글 작성 |
| GET | `/posts/{postId}/comments` | 댓글 목록 조회 |
| PUT | `/posts/{postId}/comments/{id}` | 댓글 수정 |
| DELETE | `/posts/{postId}/comments/{id}` | 댓글 삭제 |

#### 파일 (Files)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/files/temp` | 임시 파일 업로드 |
| POST | `/files/posts/{postId}/images` | 게시글 이미지 업로드 |
| GET | `/files/posts/{postId}/images` | 게시글 이미지 목록 |
| DELETE | `/files/posts/{postId}/images/{imageId}` | 이미지 삭제 |
| GET | `/files/images/{filename}` | 이미지 파일 조회 |

자세한 API 명세는 Swagger UI를 참고하세요.

---

## 🗄 데이터베이스 설계

### ERD 주요 엔티티

#### Users (사용자)
- id, name, nickname, email, password
- profile_image, role, created_at, updated_at

#### Posts (게시글)
- id, title, content, view_count
- author_id (FK → Users)
- category_id (FK → Categories)
- created_at, updated_at

#### Comments (댓글)
- id, content, deleted
- post_id (FK → Posts)
- author_id (FK → Users)
- parent_id (FK → Comments) - 대댓글 구조
- created_at, updated_at

#### Categories (카테고리)
- id, category_type (ENUM), description
- is_active

#### Post_Images (게시글 이미지)
- id, file_path, original_filename
- file_size, display_order
- post_id (FK → Posts)

---

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests UserServiceTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 테스트 구조

- **단위 테스트**: Service 레이어 (Mockito 사용)
- **통합 테스트**: Controller + Service + Repository (MockMvc)
- **테스트 Fixture**: 재사용 가능한 테스트 데이터 생성

---

## 🎯 Week 2 주요 학습 내용

### Day 8-9: 파일 업로드
- MultipartFile 처리
- 로컬 저장소 구현
- 파일 검증 (크기, 확장자)
- 보안 (경로 traversal 방지)

### Day 10-11: 동적 검색 & 정렬
- QueryDSL 설정 및 활용
- 동적 쿼리 작성 (BooleanExpression)
- Pageable을 활용한 페이징 & 정렬
- 성능 최적화 (인덱스 고려)

### Day 12-13: N+1 문제 해결
- N+1 문제 인식 및 분석
- @EntityGraph 적용
- Fetch Join (JPQL)
- BatchSize 설정
- 성능 측정 및 비교

### Day 14: 코드 리뷰 & 리팩토링
- 상수 관리 (Constants 클래스)
- 중복 코드 제거
- 예외 처리 개선
- 문서화 (README, API 명세)

---

## 📈 성능 최적화

### N+1 문제 해결 결과

**문제 상황** (게시글 10개 조회 시):
- ❌ 쿼리 21번 실행 (1 + 10*2)

**해결 후**:
- ✅ 쿼리 1번 실행 (Fetch Join)
- ✅ 95% 쿼리 감소

### 적용 기법
- Fetch Join 적용
- @EntityGraph 사용
- @BatchSize 설정

---

## 🚢 배포

### Blue-Green 배포 전략

- **Staging**: develop 브랜치 → 자동 배포 (port 8082)
- **Production**: main 브랜치 → Blue-Green 배포
    - Blue 컨테이너 (port 8080)
    - Green 컨테이너 (port 8081)
    - Nginx로 트래픽 전환
    - 무중단 배포 구현

### CI/CD 파이프라인

1. GitHub에 코드 푸시
2. GitHub Actions 트리거
3. Docker 이미지 빌드 및 GHCR 푸시
4. SSH로 EC2 접속
5. 새 컨테이너 배포 (Blue/Green)
6. Health Check
7. Nginx 설정 변경 (트래픽 전환)
8. 이전 컨테이너 종료

---

## 📝 회고

### 잘한 점
- ✅ 체계적인 도메인 구조와 레이어 분리
- ✅ 예외 처리 및 응답 표준화
- ✅ N+1 문제 해결을 통한 성능 최적화
- ✅ 테스트 코드 작성 (단위 + 통합)
- ✅ CI/CD 파이프라인 구축
- ✅ 문서화 (Swagger, README)

### 개선할 점
- 🔄 인증/인가 시스템 미구현 (Week 3 예정)
- 🔄 테스트 커버리지 향상 필요
- 🔄 로깅 전략 구체화
- 🔄 캐싱 적용 검토

### 배운 점
- Spring Boot의 핵심 기능들을 실전에서 활용하는 방법
- JPA N+1 문제의 원인과 해결 방법
- QueryDSL을 활용한 동적 쿼리 작성
- Docker와 GitHub Actions를 활용한 자동화 배포
- 프로덕션 수준의 예외 처리 및 응답 구조 설계

---

## 📞 문의
- **Blog**: https://devmee.tistory.com/
- **GitHub**: https://github.com/0802222

---

## 📄 라이선스

This project is licensed under the MIT License.