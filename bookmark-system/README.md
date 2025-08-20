# 북마크 관리 시스템

Google Drive와 연동된 개인 북마크 관리 서비스입니다.

## 기능

- **Google OAuth 인증**: Google 계정으로 로그인
- **북마크 관리**: 북마크 추가, 수정, 삭제, 조회
- **폴더 관리**: 북마크를 폴더별로 구성
- **태그 시스템**: 태그를 통한 북마크 분류
- **검색 기능**: 제목, URL, 설명, 태그로 검색
- **Google Drive 동기화**: 모든 데이터는 Google Drive에 자동 저장

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security OAuth2
- Google Drive API
- Lombok

### Frontend
- React 19
- TypeScript
- Material-UI
- React Router
- React Query
- Axios
- Vite

## 실행 방법

### 사전 준비

1. **Google Cloud Console 설정**
   - [Google Cloud Console](https://console.cloud.google.com/)에서 새 프로젝트 생성
   - Google Drive API 활성화
   - OAuth 2.0 클라이언트 ID 생성
   - 승인된 리다이렉트 URI 추가: `http://localhost:8080/login/oauth2/code/google`

2. **환경 변수 설정**
   ```bash
   export GOOGLE_CLIENT_ID=your-client-id
   export GOOGLE_CLIENT_SECRET=your-client-secret
   ```

### Backend 실행

```bash
cd bookmark-system/backend

# Windows
gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

백엔드는 http://localhost:8080 에서 실행됩니다.

### Frontend 실행

```bash
cd bookmark-system/web-app

# 의존성 설치 (최초 1회)
npm install

# 개발 서버 실행
npm run dev
```

프론트엔드는 http://localhost:3000 에서 실행됩니다.

## 사용 방법

1. 브라우저에서 http://localhost:3000 접속
2. Google 계정으로 로그인
3. 북마크 추가/관리
4. 폴더와 태그로 북마크 구성
5. 검색 기능으로 북마크 찾기

## 프로젝트 구조

```
bookmark-system/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/
│   │   └── com/bookmark/
│   │       ├── config/        # 설정 클래스
│   │       ├── controller/    # REST API 컨트롤러
│   │       ├── model/         # 도메인 모델
│   │       ├── service/       # 비즈니스 로직
│   │       └── security/      # 보안 설정
│   └── src/main/resources/
│       └── application.yml    # 애플리케이션 설정
│
└── web-app/                    # React 프론트엔드
    ├── src/
    │   ├── components/        # 재사용 가능한 컴포넌트
    │   ├── contexts/          # React Context
    │   ├── pages/             # 페이지 컴포넌트
    │   └── services/          # API 서비스
    └── package.json
```

## API 엔드포인트

### 인증
- `GET /oauth2/authorization/google` - Google OAuth 로그인
- `GET /api/auth/user` - 현재 사용자 정보
- `POST /api/auth/logout` - 로그아웃

### 북마크
- `GET /api/bookmarks` - 모든 북마크 조회
- `GET /api/bookmarks/{id}` - 특정 북마크 조회
- `POST /api/bookmarks` - 북마크 생성
- `PUT /api/bookmarks/{id}` - 북마크 수정
- `DELETE /api/bookmarks/{id}` - 북마크 삭제
- `GET /api/bookmarks/search?q={query}` - 북마크 검색

### 폴더
- `GET /api/folders` - 모든 폴더 조회
- `POST /api/folders` - 폴더 생성
- `PUT /api/folders/{id}` - 폴더 수정
- `DELETE /api/folders/{id}` - 폴더 삭제

### 태그
- `GET /api/tags` - 모든 태그 조회
- `POST /api/tags` - 태그 생성
- `DELETE /api/tags/{id}` - 태그 삭제

### Google Drive
- `GET /api/drive/init` - Drive 초기화 상태 확인
- `POST /api/drive/init` - Drive 구조 초기화
- `POST /api/sync` - Drive 동기화

## 라이선스

MIT License