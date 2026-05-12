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

JWT 기반 인증 방식을 사용합니다.

### 구현 내용

- JWT Access Token 발급
- JWT 인증 필터 구현
- Spring Security Stateless 설정
- 인증 실패 → 401 Unauthorized
- 권한 부족 → 403 Forbidden

### 주요 권한 처리

- CUSTOMER만 주문 가능
- OWNER만 가게/메뉴 관리 가능
- RIDER만 배달 상태 변경 가능
- 본인 주문만 조회 가능
- 자기 가게 주문만 관리 가능

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

- 리뷰 기능
- 라이더 배정 시스템
- 결제 기능
- 공통 예외 처리 (`@ControllerAdvice`)
- Refresh Token + Redis
- Swagger/OpenAPI 문서화
- Docker 적용
- AWS 배포

---

## 📖 프로젝트 목표

단순 CRUD 구현이 아니라:

- 실제 서비스 흐름
- 인증 / 인가
- 상태 기반 비즈니스 로직
- 객체지향 설계
- 역할 기반 권한 처리

를 직접 구현하고 학습하는 것을 목표로 진행 중입니다.