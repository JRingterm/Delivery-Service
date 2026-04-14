# 🛵 Deliver - 배달 애플리케이션 백엔드

배달 애플리케이션의 핵심 기능을 직접 설계하고 구현하는 백엔드 프로젝트입니다.

회원가입 및 로그인, 사용자 역할 분리, 주문 생성 및 처리, 결제 흐름, 배달원 매칭까지  
배달 서비스의 전체 흐름을 경험하는 것을 목표로 합니다.

---

## 🎯 프로젝트 목표

- Spring Boot 기반 REST API 설계 및 구현
- 인증/인가를 포함한 사용자 관리 시스템 구현
- 손님 / 점주 / 배달원 역할 분리
- 주문 생성부터 완료까지의 전체 흐름 구현
- 결제 및 배달 매칭 로직 경험
- 실제 서비스와 유사한 구조 설계 경험

---

## 🧩 주요 기능 (1차 구현 범위)

### 👤 사용자
- 회원가입
- 로그인
- 사용자 권한 분리 (CUSTOMER / OWNER / RIDER)

### 🏪 가게 / 메뉴
- 점주의 가게 등록
- 메뉴 등록 / 수정 / 삭제
- 손님의 가게 및 메뉴 조회

### 📦 주문
- 손님의 주문 생성
- 점주의 주문 수락 / 거절
- 주문 상태 관리

### 💳 결제
- 주문 결제 요청
- 결제 성공 처리 (Mock)
- 결제 상태 관리

### 🚴 배달
- 배달원 매칭
- 배달 상태 변경
- 주문 완료 처리

---

## 🛠 기술 스택

- Java 17
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- H2 Database (개발)
- MySQL (운영)
- Lombok
- Gradle (Groovy)

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