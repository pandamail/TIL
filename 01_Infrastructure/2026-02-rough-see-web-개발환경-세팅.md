# rough-see 프로젝트 개발 환경 셋업 가이드

> 새 PC 또는 환경에서 러프씨(rough-see) 프로젝트를 처음 셋업할 때 필요한 모든 단계.

---

## 1. 사전 준비 (필수 설치)

| 도구 | 최소 버전 | 확인 명령 |
|------|----------|----------|
| **Node.js** | 18 이상 (LTS 권장) | `node -v` |
| **npm** | Node.js에 포함 | `npm -v` |
| **Git** | 최신 | `git --version` |
| **Vercel CLI** | 최신 | `vercel --version` |
| **Supabase CLI** | 최신 | `supabase --version` |

### Vercel CLI 설치
```powershell
npm install -g vercel
```

### Supabase CLI 설치 (Windows)

> ⚠️ Supabase CLI는 npm 전역 설치(`-g`)를 공식 지원하지 않음. 바이너리 직접 다운로드 필요.

```bash
# 1) GitHub에서 바이너리 다운로드
curl -L https://github.com/supabase/cli/releases/latest/download/supabase_windows_amd64.tar.gz -o /tmp/supabase.tar.gz

# 2) 압축 해제
mkdir -p /c/Users/<사용자명>/AppData/Local/supabase
tar -xzf /tmp/supabase.tar.gz -C /c/Users/<사용자명>/AppData/Local/supabase
```

```powershell
# 3) PATH 영구 등록 (PowerShell)
$currentPath = [System.Environment]::GetEnvironmentVariable("PATH", "User")
$newPath = $currentPath + ";C:\Users\<사용자명>\AppData\Local\supabase"
[System.Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
# → 새 터미널 세션부터 자동 적용
```

---

## 2. 프로젝트 클론 및 의존성 설치

```bash
git clone https://github.com/pandamail/rough-see-web.git
cd rough-see-web
npm install
```

### Git 사용자 설정 (공용 PC 주의)

글로벌 Git 이메일이 GitHub 계정과 다르면 Vercel 자동 배포 실패 가능.

```bash
# 이 프로젝트에서만 적용 (전역 설정 영향 없음)
git config --local user.email "GitHub계정이메일"
git config --local user.name "GitHub사용자명"
```

---

## 3. 환경 변수 설정

`.env*` 파일들은 `.gitignore`에 포함되어 있어 직접 생성해야 함.

### 방법 A: Vercel CLI 자동 다운로드 (추천)

```bash
vercel link
vercel env pull .env.production.local --environment=production
vercel env pull .env.development.local --environment=preview
```

### 방법 B: 수동 생성

**`.env.local`** — 공통 변수
```env
NEXT_PUBLIC_SENTRY_DSN=Sentry_DSN_값
```

**`.env.development.local`** — dev DB (`npm run dev` 시 로드)
```env
NEXT_PUBLIC_SUPABASE_URL=dev_supabase_url
NEXT_PUBLIC_SUPABASE_ANON_KEY=dev_anon_key
```

**`.env.production.local`** — 운영 DB (`npm run build` 시 로드)
```env
NEXT_PUBLIC_SUPABASE_URL=prod_supabase_url
NEXT_PUBLIC_SUPABASE_ANON_KEY=prod_anon_key
```

### 환경별 자동 전환

| 명령어 | 연결 DB |
|--------|---------|
| `npm run dev` | dev DB |
| `npm run build` / `start` | 운영 DB |

---

## 4. Supabase DB 셋업

새 Supabase 프로젝트에서 시작하는 경우, SQL Editor에서 **순서대로** 실행:

```
1. supabase/schema.sql   → 테이블 8개 + is_admin() + RLS 정책 + Storage 버킷
2. supabase/seed.sql     → 초기 시드 데이터
```

> **관리자 계정:** `schema.sql`의 `is_admin()` 함수에 관리자 이메일 추가 필요.

---

## 5. Next.js 이미지 도메인 설정

`next.config.ts`에서 Supabase Storage 도메인 등록:

```typescript
images: {
  remotePatterns: [
    {
      protocol: "https",
      hostname: "dizjkmdsxrojboogdonn.supabase.co",  // 자신의 Supabase URL
      pathname: "/storage/v1/object/public/**",
    },
  ],
}
```

---

## 6. CLI 연결 (Vercel + Supabase)

### Vercel
```powershell
vercel login
vercel link
# scope: pandamails-projects / project: rough-see
vercel env pull .env.local
```

### Supabase
```powershell
supabase login
supabase init
supabase link --project-ref dizjkmdsxrojboogdonn
# Project ID: Supabase Dashboard → Settings → General → Reference ID
```

### 자주 쓰는 명령어
```powershell
# Vercel
vercel --prod               # 프로덕션 배포
vercel env pull .env.local  # 환경변수 다시 받기

# Supabase
supabase db push            # schema.sql → 원격 DB 반영
supabase db pull            # 원격 DB 스키마 → 로컬
supabase migration new <이름>
```

---

## 7. 개발 서버 실행

```bash
npm run dev     # localhost:3000
npm run build   # 프로덕션 빌드 테스트
npm run lint    # ESLint 검사
```

---

## 8. 기술 스택

| 영역 | 기술 | 버전 |
|------|------|------|
| Language | TypeScript | 5.x |
| Framework | Next.js (App Router) | 16.x |
| UI | React | 19.x |
| Styling | Tailwind CSS | 4.x |
| DB/Auth/Storage | Supabase | 2.x |
| WYSIWYG Editor | TipTap | 3.x |
| Error Monitoring | Sentry | 10.x |
| Deployment | Vercel | - |

---

## 9. 배포 정보

- **URL:** https://rough-see.vercel.app/
- **배포 방식:** GitHub main 브랜치 push 시 자동 배포
