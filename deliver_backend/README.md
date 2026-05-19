# 🛵 Deliver - 배달 애플리케이션 백엔드

배달 애플리케이션의 핵심 기능을 직접 설계하고 구현하는 백엔드 프로젝트입니다.

회원가입 및 로그인, 사용자 역할 분리, 가게 및 메뉴 관리, 주문 생성 및 처리,  
배달 상태 흐름 관리까지 배달 서비스의 핵심 흐름을 구현하는 것을 목표로 합니다.

---

## 🎯 프로젝트 목표

- Spring Boot 기반 REST API 설계 및 구현
- JWT 기반 인증 / 인가 시스템 구현
- CUSTOMER / OWNER / RIDER 역할 분리
- 주문 생성부터 배달 완료까지의 전체 흐름 구현
- 상태 기반 비즈니스 로직 설계 경험
- 실제 서비스와 유사한 구조 설계 경험

---

## 🧩 주요 기능

### 👤 사용자 (User)

- 회원가입
- 로그인
- JWT Access Token 발급
- 사용자 권한 분리

#### 역할(Role)

```text
CUSTOMER
OWNER
RIDER
```

---

### 🏪 가게 (Store)

- 점주의 가게 등록
- 가게 목록 조회
- 가게 단건 조회

#### 권한 정책

- OWNER만 가게 생성 가능

---

### 🍕 메뉴 (Menu)

- 메뉴 등록
- 가게별 메뉴 조회
- 메뉴 단건 조회

#### 권한 정책

- OWNER만 메뉴 등록 가능
- 자신의 가게에만 메뉴 등록 가능

---

### 📦 주문 (Order)

- 주문 생성
- 내 주문 목록 조회
- 내 주문 단건 조회

#### 주문 생성 규칙

- CUSTOMER만 주문 가능
- 같은 가게 메뉴만 함께 주문 가능

---

### 👨‍🍳 점주 주문 관리 (Owner Order)

- 자기 가게 주문 목록 조회
- 자기 가게 주문 단건 조회
- 주문 수락 / 거절
- 조리 시작
- 배달 준비 완료 처리

---

### 🚴 배달 (Rider)

- 주문 픽업
- 배달 완료 처리

#### 권한 정책

- RIDER만 배달 상태 변경 가능

---

### 🔍 검색 / 페이징

- Pageable 기반 페이징 처리
- QueryDSL 기반 동적 검색 구현
- 리뷰 검색 및 정렬
- 주문 검색 및 정렬
- 가게 검색 및 정렬

#### 지원 기능

- 평점 범위 검색
- 주문 금액 범위 검색
- 키워드 검색
- 정렬 조건 처리
- 잘못된 정렬 요청 방어 처리

## 🔄 주문 상태 흐름

```text
CREATED
→ ACCEPTED
→ COOKING
→ READY_FOR_DELIVERY
→ DELIVERING
→ COMPLETED
```

또는

```text
CREATED
→ REJECTED
```

---

## 🔐 인증 / 인가

JWT + Redis 기반 인증 방식을 사용합니다.

### 구현 내용

- JWT Access Token 발급
- JWT Refresh Token 발급
- Redis 기반 Refresh Token 저장 및 관리
- Access Token 재발급 API 구현
- 로그아웃 시 Refresh Token 삭제
- JWT 인증 필터 구현
- Spring Security Stateless 설정
- Swagger JWT 인증 연동
- 인증 실패 → 401 Unauthorized
- 권한 부족 → 403 Forbidden


### 인증 흐름

#### 로그인 성공 시

```text
- Access Token 발급
- Refresh Token 발급
- Refresh Token Redis 저장
```
#### Access Token 만료 시:
```text
Refresh Token 검증
→ Redis 저장값 비교
→ 새 Access Token 발급
→ 새 Refresh Token 발급 및 Redis 갱신
```
#### 로그아웃 시:
```text
Redis Refresh Token 삭제
→ 재발급 차단
```

### Redis 사용 목적
- Refresh Token 저장
- 로그인 상태 관리
- 로그아웃 처리
- Refresh Token 재사용 방지
- 최신 Refresh Token만 허용

### 주요 권한 처리

- CUSTOMER만 주문 가능
- OWNER만 가게/메뉴 관리 가능
- RIDER만 배달 상태 변경 가능
- 본인 주문만 조회 가능
- 자기 가게 주문만 관리 가능


---

## 🐳 Docker

Docker Compose를 사용하여 Spring Boot, MySQL, Redis 환경을 컨테이너 기반으로 구성하였습니다.

Docker 환경에서는 `SPRING_PROFILES_ACTIVE=docker` 설정을 통해  
`application-docker.yml` 설정 파일이 함께 적용됩니다.

---

### 📦 컨테이너 구성

| Container | Description |
|---|---|
| app | Spring Boot 애플리케이션 |
| mysql | MySQL 8 데이터베이스 |
| redis | Redis 7 (Refresh Token 저장소) |

---

### ⚙ 실행 방법

#### 1. 애플리케이션 빌드

Linux / Mac

```bash
./gradlew clean build
```

Windows PowerShell

```powershell
.\gradlew clean build
```

---

#### 2. Docker Compose 실행

```bash
docker compose up --build -d
```

백그라운드(detached mode)로 컨테이너를 실행합니다.

---

### 🌐 접속 정보

| Service | URL / Port                                  |
|---|---------------------------------------------|
| Application | http://localhost:8080                       |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| MySQL | localhost:3308                              |
| Redis | localhost:6379                              |

MySQL의 디폴트 포트인 3306은 로컬에서 사용중이라 3308로 대체했습니다.

---

### 🔍 컨테이너 상태 확인

전체 컨테이너 확인:

```bash
docker ps
```

전체 로그 확인:

```bash
docker compose logs -f
```

app 컨테이너 로그 확인:

```bash
docker compose logs -f app
```

---

### 🛑 컨테이너 종료

```bash
docker compose down
```

MySQL 데이터는 named volume(`mysql-data`)을 통해 유지됩니다.

---

### 🗑 컨테이너 + 볼륨 완전 삭제

```bash
docker compose down -v
```

MySQL 데이터까지 함께 삭제됩니다.

---

### 🧩 Docker 환경 구성 목적

- Spring Boot 실행 환경 컨테이너화
- MySQL / Redis 의존성 분리
- 개발 환경 일관성 확보
- Docker Compose 기반 통합 실행 환경 구성
- Refresh Token 저장소 Redis 분리 운영

---
## 🛠 기술 스택

### Backend

- Java 17
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- JWT (jjwt)

### Database

- H2 Database (개발)
- MySQL (운영)

### Build Tool

- Gradle (Groovy)

### ETC

- Lombok
- QueryDSL
- Redis
- Swagger / OpenAPI
- Docker
- Docker Compose

---

## 🏗 프로젝트 구조

본 프로젝트는 역할과 책임을 분리하기 위해  
**Entity - Repository - Service - DTO - Controller 구조**를 기반으로 설계합니다.

```text
src/main/java/com/example/deliver
├─ domain
│  ├─ user
│  │  ├─ entity
│  │  ├─ repository
│  │  ├─ service
│  │  ├─ dto
│  │  └─ controller
│  ├─ store
│  ├─ menu
│  ├─ order
│  ├─ payment
│  └─ delivery
└─ global
   ├─ config
   ├─ security
   ├─ exception
   └─ response
```

---

## 🗄 JPA 연관관계 설계

```text
User 1 : N Store
Store 1 : N Menu
User 1 : N Order
Store 1 : N Order
Order 1 : N OrderItem
Menu 1 : N OrderItem
```

---

## 🔁 양방향 연관관계 관리

`Order`와 `OrderItem`은 양방향 연관관계로 설계하였습니다.

```java
public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
}
```

### 설계 이유

양방향 연관관계에서는:

- 객체 그래프 일관성 유지
- DB 외래키 정상 저장

을 모두 만족해야 하기 때문입니다.

---

## ⚡ 성능 최적화

### EntityGraph 사용

N+1 문제를 방지하기 위해 `@EntityGraph`를 사용하였습니다.

```java
@EntityGraph(attributePaths = {
    "store",
    "customer",
    "orderItems",
    "orderItems.menu"
})
```

---

### Fetch Join 최적화

QueryDSL의 `fetch join`을 활용하여 연관 엔티티를 한 번의 조회로 가져오도록 최적화하였습니다.

```java
.leftJoin(order.store).fetchJoin()
.leftJoin(order.customer).fetchJoin()
.leftJoin(order.orderItems, orderItem).fetchJoin()
.leftJoin(orderItem.menu).fetchJoin()
```

---

### 컬렉션 Fetch Join + Pageable 문제 해결

컬렉션 fetch join과 Pageable을 함께 사용할 경우 발생할 수 있는 문제를 해결하기 위해:

1. ID 목록 조회
2. Fetch Join 조회

의 2단계 조회 전략을 적용하였습니다.

#### 적용 효과

- N+1 문제 해결
- 중복 데이터 최소화
- 페이징 안정성 확보
- 조회 성능 개선

---

## 📌 주요 API 예시

### 회원가입

```http
POST /api/users/signup
```

### 로그인

```http
POST /api/users/login
```

### 가게 생성

```http
POST /api/stores
```

### 메뉴 등록

```http
POST /api/stores/{storeId}/menus
```

### 주문 생성

```http
POST /api/orders
```

### 점주 주문 수락

```http
PATCH /api/owner/orders/{orderId}/accept
```

### 배달 시작

```http
PATCH /api/rider/orders/{orderId}/pickup
```

### 배달 완료

```http
PATCH /api/rider/orders/{orderId}/complete
```

---

## 📚 주요 학습 내용

### Spring Security

- JWT 인증 흐름
- Authentication / Authorization
- AuthenticationPrincipal 활용
- Stateless 인증 구조

### JPA

- 연관관계 매핑
- 지연 로딩(LAZY)
- Cascade
- EntityGraph
- 양방향 연관관계 관리
- QueryDSL 동적 쿼리 작성
- Pageable 기반 페이징 처리
- Fetch Join 최적화
- 컬렉션 Fetch Join + Pageable 처리 전략

### 비즈니스 로직

- 주문 상태 흐름(State Transition)
- 역할 기반 권한 검증
- 리소스 소유권 검사

### API 설계

- RESTful API 설계
- 역할 기반 API 분리
- 상태 기반 로직 설계

---

## 🚀 향후 개선 예정

- 라이더 자동 배정 시스템
- 결제 기능
- CI/CD 구축
- AWS 배포
- 모니터링 시스템 구축

---

## 📖 프로젝트 목표

단순 CRUD 구현이 아니라:

- 실제 서비스 흐름
- 인증 / 인가
- 상태 기반 비즈니스 로직
- 객체지향 설계
- 역할 기반 권한 처리

를 직접 구현하고 학습하는 것을 목표로 진행 중입니다.