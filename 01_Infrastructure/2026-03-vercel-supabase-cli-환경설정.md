# Vercel CLI + Supabase CLI 환경 설정 (Windows)

> 출처: rough-see-web day4 작업 일지 (2026-03-23)  
> 목표: VSCode에서 Claude Code를 통해 Supabase DB 및 Vercel 배포를 CLI로 직접 관리

---

## 1. Vercel CLI 설치 및 연결

### 설치
```powershell
npm install -g vercel
# → Vercel CLI 50.35.0 설치 완료
```

### 프로젝트 연결
```powershell
vercel login
# → 브라우저 OAuth 인증

vercel link
# → .env.local 자동 생성 (Vercel Development 환경변수 다운로드)
```

`.env.local`에 자동 다운로드된 변수:
- `NEXT_PUBLIC_SENTRY_DSN`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `NEXT_PUBLIC_SUPABASE_URL`
- `VERCEL_OIDC_TOKEN`

---

## 2. Supabase CLI 설치 (Windows 트러블슈팅 포함)

### 시도 1 — npm 전역 설치 (실패)
```powershell
npm install -g supabase
# 오류: "Installing Supabase CLI as a global module is not supported."
# 원인: Supabase CLI는 Go 바이너리. npm 전역 설치 미지원.
```

### 시도 2 — winget 설치 (실패)
```powershell
winget install Supabase.CLI
# 오류: 입력 조건과 일치하는 패키지를 찾을 수 없습니다.
```

### 시도 3 — GitHub 바이너리 직접 다운로드 (성공)
```bash
# 바이너리 다운로드
curl -L https://github.com/supabase/cli/releases/latest/download/supabase_windows_amd64.tar.gz -o /tmp/supabase.tar.gz

# 압축 해제
mkdir -p /c/Users/Administrator/AppData/Local/supabase
tar -xzf /tmp/supabase.tar.gz -C /c/Users/Administrator/AppData/Local/supabase

# 버전 확인
supabase --version  # → 2.78.1
```

### PATH 영구 등록 (PowerShell)
```powershell
$currentPath = [System.Environment]::GetEnvironmentVariable("PATH", "User")
$newPath = $currentPath + ";C:\Users\Administrator\AppData\Local\supabase"
[System.Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
# → 다음 터미널 세션부터 자동 적용
```

### 프로젝트 연결
```powershell
supabase init
supabase link --project-ref dizjkmdsxrojboogdonn
# → rough-see 프로덕션 프로젝트 연결 완료
```

---

## 3. 사용 가능한 CLI 명령어

### Vercel
```powershell
vercel --prod                    # 프로덕션 배포
vercel env pull .env.local       # 환경변수 다시 받기
vercel logs                      # 배포 로그 확인
vercel ls                        # 배포 목록 확인
```

### Supabase
```powershell
supabase db diff                 # 로컬 스키마 변경사항 확인
supabase db push                 # 스키마를 원격 DB에 적용
supabase db pull                 # 원격 DB 스키마를 로컬로 가져오기
supabase migration new <이름>   # 새 마이그레이션 파일 생성
supabase status                  # 연결된 프로젝트 상태 확인
```

---

## 4. 주의사항

- **현재 세션에서 supabase 명령어가 안 될 경우:** 새 PowerShell 터미널을 열면 PATH 자동 적용
- **연결된 Supabase 프로젝트:** `rough-see` (Project ID: `dizjkmdsxrojboogdonn`) — 프로덕션 DB
- **`supabase db push` 주의:** 프로덕션 DB에 직접 반영되므로 schema.sql 수정 후 신중하게 실행
