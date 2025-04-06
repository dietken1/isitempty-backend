# 🚗 IsItempty 백엔드 개발 가이드

이 문서는 IsItempty 백엔드 애플리케이션의 로컬 개발 환경 설정 및 실행 방법을 안내합니다.

## 📋 목차

- [개발 환경 설정](#개발-환경-설정)
- [애플리케이션 실행](#애플리케이션-실행)
- [데이터베이스 연결 설정](#데이터베이스-연결-설정)
- [환경 변수 설정](#환경-변수-설정)
- [프로필 설정](#프로필-설정)
- [API 테스트](#api-테스트)
- [문제 해결](#문제-해결)
- [프로젝트 구조](#프로젝트-구조)
- [참고 자료](#참고-자료)

## 💻 개발 환경 설정

### 필수 요구사항

- ✅ JDK 17 이상
- ✅ MySQL (SSH 터널링 방식 사용)
- ✅ Git

### 프로젝트 클론

```bash
git clone https://github.com/isitempty/backend.git
cd backend
```

## 🚀 애플리케이션 실행

### IntelliJ IDEA에서 실행

1. IntelliJ IDEA에서 프로젝트 열기
2. `IsitemptyApplication.java` 실행 버튼 클릭

### 명령줄에서 실행

```bash
./gradlew bootRun
```

## 🔌 데이터베이스 연결 설정

### 방법 1: SSH 터널링을 통한 원격 데이터베이스 연결 (✅ 권장)

1. 스크립트를 사용한 SSH 터널 설정
   ```bash
   ./backend/scripts/setup-ssh-tunnel.sh
   ```

2. 비밀번호 입력 요청 시 `A5$erNy!=Lu` 입력

3. SSH 터널링이 설정되면, 애플리케이션은 `localhost:3307`을 통해 원격 MySQL 서버에 연결됩니다.

### 방법 2: 로컬 MySQL 사용 (⚠️ 권장하지 않음)

1. 로컬 MySQL 서버 설치 및 실행

2. `isitempty` 데이터베이스 생성

3. `application-dev.properties` 파일에서 데이터베이스 연결 정보 설정

4. 애플리케이션 실행 시 `-Dspring.profiles.active=dev` 옵션 추가

## 🔐 환경 변수 설정

`.env` 파일을 프로젝트 루트에 생성하고 다음 내용을 추가합니다:

```bash
DB_USERNAME=
DB_PASSWORD=
SSH_HOST=
SSH_PORT=
SSH_USERNAME=
SSH_PASSWORD=
SSH_REMOTE_HOST=
SSH_REMOTE_PORT=
SSH_LOCAL_PORT=
```

## 🔄 프로필(개발 환경) 설정

애플리케이션은 다음 프로필을 지원합니다:

| 프로필 | 설명 | 권장 여부 |
|--------|------|-----------|
| `serverdb` | 원격 데이터베이스 연결 (SSH 터널링 사용) | ✅ |
| `prod` | 운영 환경 | - |
| `dev` | 로컬 MySQL 사용 | ❌ |

## 🧪 API 테스트

애플리케이션이 실행되면 다음 URL로 API를 테스트할 수 있습니다:

- 헬로 월드: [http://localhost:8080/api/hello](http://localhost:8080/api/hello)
- 주차장 목록: [http://localhost:8080/api/parking-lots](http://localhost:8080/api/parking-lots)

## ⚠️ 문제 해결

### 1. SSH 터널링 오류

오류 메시지: `PortForwardingL: local port 127.0.0.1:3307 cannot be bound.`

**[해결 방법]**

1. 이미 실행 중인 SSH 터널이 있는지 확인
   ```bash
   ps aux | grep ssh
   ```

2. 다른 프로세스가 3307 포트를 사용 중인지 확인
   ```bash
   lsof -i :3307
   ```

3. 다른 로컬 포트 사용 (예: 3308)
   ```bash
   ssh -L 3308:localhost:3306 root@223.130.134.121
   ```

### 2. 데이터베이스 연결 오류

오류 메시지: `Communications link failure`

**[해결 방법]**

1. SSH 터널이 활성화되어 있는지 확인
   ```bash
   ps aux | grep ssh
   ```

2. 데이터베이스 자격 증명이 올바른지 확인
   ```bash
   mysql -h 127.0.0.1 -P 3307 -u isitempty -p
   ```

## 📁 프로젝트 구조

```
backend/
├── src/main/java/com/isitempty/
│   ├── IsitemptyApplication.java      # 애플리케이션 진입점
│   ├── config/
│   │   ├── SshTunnelConfig.java       # SSH 터널링 설정
│   │   ├── EnvTest.java               # 환경 변수 테스트
│   │   └── EnvTest2.java              # 시스템 환경 변수 테스트
│   └── backend/
│       ├── hello/
│       │   └── HelloController.java   # 테스트용 컨트롤러
│       └── parkinglot/
│           ├── controller/            # 주차장 API 컨트롤러
│           ├── model/                 # 주차장 데이터 모델
│           ├── repository/            # 주차장 데이터 접근 계층
│           └── service/               # 주차장 비즈니스 로직
├── src/main/resources/
│   ├── application.properties         # 기본 애플리케이션 설정
│   ├── application-dev.properties     # 개발 환경 설정
│   └── application-prod.properties    # 운영 환경 설정
└── scripts/
    └── setup-ssh-tunnel.sh           # SSH 터널 설정 스크립트
```

## 📚 참고 자료

- [Spring Boot 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MySQL 문서](https://dev.mysql.com/doc/)
- [SSH 터널링 가이드](https://www.ssh.com/academy/ssh/tunneling)

---

© 2025 IsItempty Team