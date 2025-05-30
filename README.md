# 🚗 IsItEmpty 백엔드

<div align="center">
  <img src="./images/logo.png" alt="IsItEmpty Logo" width="400"/>
  
  > 🅿️ 실시간 주차장 빈자리 확인으로 주차 스트레스 해소!
</div>

서울시 공공데이터를 활용한 실시간 주차장 빈자리 확인 서비스의 백엔드 시스템입니다.

## 💡 서비스 소개

IsItEmpty는 다음과 같은 문제를 해결하기 위해 만들어졌습니다:

- 🚫 불법 주정차로 인한 사회적 문제
- 😫 주차 공간을 찾기 위한 불필요한 시간 낭비
- 🌍 교통 체증과 환경 오염

### 주요 기능

1. 🔍 **실시간 주차장 정보**
   - 서울시 시영주차장 실시간 정보 제공
   - 빈자리 수, 운영 시간, 요금 정보 제공
   - 위치 기반 주변 주차장 검색

2. 📸 **주차 단속 카메라 정보**
   - 주차 단속 카메라 위치 정보 제공
   - 단속 구역 안내

3. 🚽 **편의시설 정보**
   - 주차장 내 화장실 위치
   - 장애인 편의시설 정보

4. ⭐ **사용자 맞춤 기능**
   - 즐겨찾기 주차장 등록
   - 리뷰 및 평점 시스템
   - 주차장 이용 내역 관리

5. 👑 **관리자 기능**
   - 사용자 관리 (조회/수정/삭제)
   - 권한 관리 (관리자/일반 사용자)
   - 문의사항 관리
   - 시스템 모니터링

## 🛠 기술 스택

- **언어 및 프레임워크**: 
  - ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) 
  - ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)

- **데이터베이스**: 
  - ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
  - ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)

- **인증/인가**: 
  - ![OAuth2.0](https://img.shields.io/badge/OAuth2.0-2C5BB4?style=flat-square)
  - ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white)

- **빌드 및 배포**: 
  - ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
  - ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
  - ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=github-actions&logoColor=white)

## 📐 시스템 아키텍처

### 전체 구조
```
isitempty_backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.isitempty/
│   │   │       ├── backend/
│   │   │       │   ├── camera/        # 단속 카메라 관리
│   │   │       │   ├── favorite/      # 즐겨찾기 기능
│   │   │       │   ├── oauthlogin/    # 소셜 로그인
│   │   │       │   ├── parkinglot/    # 주차장 관리
│   │   │       │   ├── question/      # 문의사항
│   │   │       │   ├── review/        # 리뷰 시스템
│   │   │       │   └── toilet/        # 화장실 정보
│   │   │       └── config/            # 시스템 설정
│   │   └── resources/                 # 리소스 파일
│   └── test/                          # 테스트 코드
└── build.gradle                       # 빌드 설정
```

### 주요 컴포넌트

1. 🔐 **인증/인가 시스템**
   - OAuth2.0 기반 소셜 로그인 (Google, Naver, Kakao)
   - JWT 토큰 기반 인증
   - 사용자 권한 관리 (USER, ADMIN)

2. 🅿️ **주차장 관리 시스템**
   - 실시간 주차장 정보 조회
   - 주차장 검색 및 필터링
   - 즐겨찾기 기능

3. ⭐ **리뷰 시스템**
   - 주차장 리뷰 작성/수정/삭제
   - 리뷰 평점 관리

4. 💾 **데이터 캐싱**
   - Redis를 활용한 실시간 데이터 캐싱
   - 성능 최적화

5. 🔒 **보안 설정**
   - CORS 설정
   - Spring Security 기반 보안 구성
   - SSH 터널링 지원

## 🔌 API 엔드포인트

### 인증
- 🔑 POST `/api/auth/login`: 로그인
- ✨ POST `/api/auth/signup`: 회원가입
- 👤 GET `/api/auth/me`: 현재 사용자 정보 조회

### 주차장
- 📋 GET `/api/parkinglots`: 주차장 목록 조회
- 🔍 GET `/api/parkinglots/{id}`: 특정 주차장 정보 조회
- ⚡ GET `/api/parkinglots/realtime`: 실시간 주차장 정보 조회

### 리뷰
- ✏️ POST `/api/reviews`: 리뷰 작성
- 📝 PUT `/api/reviews/{id}`: 리뷰 수정
- 🗑️ DELETE `/api/reviews/{id}`: 리뷰 삭제

### 즐겨찾기
- ⭐ POST `/api/favorites`: 즐겨찾기 추가
- 💫 DELETE `/api/favorites/{id}`: 즐겨찾기 삭제
- 📑 GET `/api/favorites`: 즐겨찾기 목록 조회

### 관리자
- 👥 GET `/api/admin/users`: 사용자 목록 조회
- 🔄 PUT `/api/admin/users/{id}`: 사용자 정보 수정
- 🗑️ DELETE `/api/admin/users/{id}`: 사용자 삭제
- 👑 PATCH `/api/admin/users/{id}/role`: 사용자 권한 변경

## ⚙️ 개발 환경 설정

1. 필수 요구사항
   - ☕ JDK 17 이상
   - 🐬 MySQL 8.0
   - 🔄 Redis
   - 🐳 Docker (선택사항)

2. 환경변수 설정
   ```properties
   # application-dev.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/isitempty
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   spring.data.redis.password=your_redis_password
   
   seoul.key=your_seoul_api_key
   ```

3. 빌드 및 실행
   ```bash
   # 프로젝트 빌드
   ./gradlew build
   
   # 애플리케이션 실행
   ./gradlew bootRun
   ```

4. Docker 실행
   ```bash
   # Docker 이미지 빌드
   docker build -t isitempty-backend .
   
   # Docker 컨테이너 실행
   docker-compose up
   ```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.isitempty.backend.YourTest"
```

## 🚀 배포

GitHub Actions를 통한 자동 배포가 구성되어 있습니다:
1. main 브랜치에 push 시 자동 빌드
2. 테스트 실행
3. Docker 이미지 빌드 및 푸시
4. 운영 서버 배포

## 📊 모니터링

- 📝 로깅 레벨 설정을 통한 모니터링
- 📈 실시간 데이터 처리 상태 모니터링
- 🚨 에러 로깅 및 추적

## 📜 라이선스

This project is licensed under the MIT License - see the LICENSE file for details

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 연락처

프로젝트 관련 문의사항은 아래 채널을 통해 연락주세요:

- 이메일: dietken1@ajou.ac.kr
- 웹사이트: https://isitempty.kr
- GitHub: https://github.com/isitempty