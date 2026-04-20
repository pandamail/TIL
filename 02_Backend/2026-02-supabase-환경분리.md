# Supabase 환경 분리 가이드 (dev / prod)

> 출처: rough-see-web `docs/supabase-env-separation.md`  
> 배경: 단일 Supabase 프로젝트로 개발/운영을 함께 쓰는 구조에서 환경 분리로 전환

---

## 현재 상태 (분리 전)

- 단일 Supabase 프로젝트로 개발/운영 공용
- 환경변수: `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`

---

## 권장 구조

```
Production (main 브랜치)
├── Supabase Project: rough-see-prod
├── Vercel Environment: Production
└── .env: PROD Supabase URL/KEY

Development (dev 브랜치)
├── Supabase Project: rough-see-dev
├── Vercel Environment: Preview
└── .env.local: DEV Supabase URL/KEY
```

---

## 설정 단계

### 1. Supabase 개발용 프로젝트 생성

1. `supabase.com` > New Project
2. 이름: `rough-see-dev`, 같은 organization 내 생성
3. `supabase/schema.sql` 실행 → 테이블 생성
4. `supabase/seed.sql` 실행 → 초기 데이터 삽입

### 2. Vercel 환경변수 설정

Vercel Dashboard > Settings > Environment Variables:

| 변수 | Production | Preview | Development |
|------|-----------|---------|-------------|
| `NEXT_PUBLIC_SUPABASE_URL` | prod URL | dev URL | dev URL |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | prod KEY | dev KEY | dev KEY |
| `NEXT_PUBLIC_SENTRY_DSN` | DSN | DSN | (비우기) |

### 3. 로컬 개발 환경 (`npm run dev`)

`.env.development.local` — dev Supabase를 가리키도록:
```env
NEXT_PUBLIC_SUPABASE_URL=https://xxxxx.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJxxx...
```

### 4. Storage 버킷 설정

개발용 프로젝트에서도 동일하게:
1. Storage > New bucket > `portfolio-images` (public)
2. RLS 정책 적용 (`schema.sql`의 Storage 섹션)

### 5. Auth 설정

개발용 프로젝트:
1. Authentication > Settings > Email auth 활성화
2. Dashboard > Users > Add user로 관리자 계정 생성

---

## 주의사항

- 개발 DB의 데이터는 테스트용 — 운영 데이터와 분리
- 새 테이블/컬럼 추가 시 **양쪽 모두**에 SQL 실행 필요
- `schema.sql`을 항상 최신 상태로 유지 (단일 진실의 원천)
- 개발용 Storage에 올린 이미지는 운영에서 보이지 않음

---

## 환경 분리 전 최소 보안 조치

환경 분리가 아직 어려운 경우 현재 단일 프로젝트에서:

1. **RLS 강화** — `is_admin()` 함수로 관리자만 쓰기 가능
   ```sql
   CREATE OR REPLACE FUNCTION public.is_admin()
   RETURNS BOOLEAN AS $$
   BEGIN
     RETURN (
       auth.role() = 'authenticated'
       AND (auth.jwt() ->> 'email') IN ('관리자이메일')
     );
   END;
   $$ LANGUAGE plpgsql SECURITY DEFINER;
   ```

2. **Supabase Dashboard > Authentication > Settings:**
   - "Allow new users to sign up" 비활성화 (외부인 회원가입 차단)

3. **정기적 DB 백업** — Supabase Dashboard > Database > Backups
