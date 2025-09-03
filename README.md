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