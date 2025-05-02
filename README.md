# 🚗 IsItempty 백엔드 개발 가이드

이 문서는 IsItempty 백엔드 애플리케이션의 로컬 개발 환경 설정 및 실행 방법을 안내합니다.

## 📋 목차

- [개발 환경 설정](#개발-환경-설정)
- [애플리케이션 실행](#애플리케이션-실행)
- [데이터베이스 연결 설정](#데이터베이스-연결-설정)
- [환경 설정 파일](#환경-설정-파일)
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
- ✅ Intellij IDE (권장)

### 프로젝트 클론

```bash
git clone https://github.com/isitempty/backend.git
cd backend
```

## 🚀 애플리케이션 실행

### 1) IntelliJ IDEA에서 실행

1. IntelliJ IDEA에서 프로젝트 열기
2. `IsitemptyApplication.java` 실행 버튼 클릭

### 2) 명령줄에서 실행

```bash
./gradlew bootRun
```

## 🔌 데이터베이스 연결 설정

### SSH 터널링을 통한 원격 데이터베이스 연결
1. Bash 스크립트 파일에 권한 부여
   ```bash
   $ chmod +x mysql-tunnel.sh redis-tunnel.sh
   ```

2. 터미널 2개를 열고 각각 Bash 스크립트 파일을 실행 후, 서버 비밀번호를 입력
   ```bash
   // Mysql
   $ ./mysql-tunnel.sh
   
   // Redis
   $ ./redis-tunnel.sh
   ```

> ✅ **SSH 터널링이 설정되면 애플리케이션은 원격 MySQL 및 Redis 서버에 연결됩니다.**

## 🔐 환경 설정 파일

### 애플리케이션 프로필

프로젝트는 Spring Boot의 프로필 기능을 사용하여 환경별 설정을 관리합니다:

1. **application.properties**: 공통 설정
2. **application-dev.properties**: 개발 환경 설정 (민감 정보 포함)
3. **application-prod.properties**: 운영 환경 설정

### 개발 환경 설정하기

1. `src/main/resources/application-dev.properties.example` 파일을 복사하여 같은 위치에 `application-dev.properties` 파일을 생성합니다.
2. 생성한 파일에서 다음 설정값을 실제 값으로 변경합니다:
   ```properties
   # 개발 환경 설정

   # MySQL
   spring.datasource.url=
   spring.datasource.username=
   spring.datasource.password=
   
   # Redis
   spring.data.redis.host=
   spring.data.redis.port=
   spring.data.redis.password=
   
   # 서울시 API 키
   seoul.key=
   
   # SSH 터널링 설정
   ssh.host=
   ssh.port=
   ssh.username=
   ssh.password=
   ssh.remote.host=
   ssh.remote.port=
   ssh.local.port=
   ssh.enabled=
   
   # Redis 터널링 설정
   ssh.redis.remote.host=
   ssh.redis.remote.port=
   ssh.redis.local.port=
   ```

> **중요**: `application-dev.properties` 파일은 `.gitignore`에 추가되어 있어 Git에 커밋되지 않습니다. .env의 역할을 한다고 생각하면 됩니다.

## 🔄 프로필(개발 환경)

애플리케이션은 다음 프로필을 지원합니다:

| 프로필 | 설명 |
|--------|------|
| `dev` | 개발 환경 (기본값) |
| `prod` | 운영 환경 |

## 🧪 API 테스트

애플리케이션이 실행되면 다음 URL로 API를 테스트할 수 있습니다:

- 주차장 목록: [http://localhost:8080/api/parking-lots](http://localhost:8080/api/parking-lots)

## ⚠️ 문제 해결

### 1. SSH 터널링 오류

오류 메시지: `PortForwardingL: local port 3307 cannot be bound.`

**[해결 방법]**

1. 이미 실행 중인 SSH 터널이 있는지 확인
   ```bash
   $ ps aux | grep ssh
   ```

2. 다른 프로세스가 3307 포트를 사용 중인지 확인
   ```bash
   $ lsof -i :3307
   ```

3. 다른 로컬 포트 사용 (예: 3308)
   ```bash
   $ ssh -L 3307:172.19.0.3:3306 root@223.130.134.121
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