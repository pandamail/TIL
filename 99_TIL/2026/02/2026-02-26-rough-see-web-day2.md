# Day 2 작업 일지 (2026-02-26)

## 작업 개요
**오전:** 가격표 태그 기능, 컬러 팔레트 전면 변경, 대규모 코드 리팩토링  
**오후:** 히어로 배너 슬라이더, 홈페이지 팝업, 포트폴리오 갤러리 슬라이더 변경, 가격표 바로가기  
**저녁:** Notice 페이지 리디자인, SEO 최적화, Vercel Analytics, Search Console 등록

---

## 오전 작업

### 1. 가격표 태그 기능 (HOT / EVENT / NEW / BEST)

```sql
ALTER TABLE pricing_items ADD COLUMN tag TEXT DEFAULT NULL;
```

- Admin 가격표 관리에서 각 항목에 태그 지정/해제 (드롭다운)
- 공개 가격표 페이지에서 상품명 옆에 컬러 뱃지 표시
  - HOT: 빨간색, EVENT: 보라색, NEW: 초록색, BEST: 파란색
  - `pulse-subtle` 애니메이션으로 주목도 향상

### 2. 컬러 팔레트 전면 변경

따뜻한 브라운/베이지 톤 → **쿨톤 블루/그레이** 팔레트로 전면 교체.

| 변수명 | 변경 전 | 변경 후 | 용도 |
|--------|---------|---------|------|
| `--color-muted-brown` | `#A09080` | `#7B91A8` | 포인트/링크 |
| `--color-sky` | — | `#BFD1E1` | **신규** 서브 포인트 |
| `--color-highlight` | — | `#FCF5BC` | **신규** 강조 배경 |

> Tailwind CSS 변수명은 유지하고 값만 변경 → 모든 컴포넌트 자동 적용.

### 3. 대규모 리팩토링

- **타입 중앙화:** `src/types/index.ts` 신규 — 7개 공유 타입 한 파일 관리
- **상수 중앙화:** `src/constants/index.ts` 신규 — `STORAGE_BUCKET`, `SOCIAL_LINKS`, `BOARD_TYPE_STYLES` 등
- **미사용 파일 삭제:** `PortfolioModal.tsx`, `dummy-portfolios.ts`, `dummy-boards.ts`
- **SQL 통합:** 6개 → 2개 (`schema.sql` + `seed.sql`)

---

## 오후 작업

### 4. 히어로 배너 슬라이더

```sql
CREATE TABLE hero_banners (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  image_urls TEXT[] NOT NULL DEFAULT '{}',
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

- `AnimatePresence`로 페이드/슬라이드 전환, 4초 자동 재생, 하단 인디케이터 도트
- Admin: 다중 이미지 업로드, 화살표로 순서 변경, 개별 삭제, 미리보기

### 5. 홈페이지 이벤트 팝업

```sql
CREATE TABLE popup_banners (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  image_url TEXT NOT NULL DEFAULT '',
  link_url TEXT NOT NULL DEFAULT '',
  is_active BOOLEAN NOT NULL DEFAULT false,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

- "오늘 하루 안 보기": `localStorage`에 타임스탬프 저장, 24시간 이내 재방문 시 미표시

### 6. 가격표 바로가기

- 포트폴리오 상세에서 "가격표 보러가기" → `/pricing?category=카테고리명`
- **Fuzzy 매칭:** "베이비 스냅" → "베이비" → "베이비 & 가족" 섹션 매칭
- 매칭 섹션으로 smooth scroll + 2.5초 ring 하이라이트

---

## 저녁 작업

### 7. Notice 페이지 리디자인

```sql
ALTER TABLE boards ADD COLUMN IF NOT EXISTS thumbnail_url TEXT NOT NULL DEFAULT '';
```

- **공지사항:** 아코디언 (`AnimatePresence` expand/collapse)
- **이벤트:** `grid-cols-1 sm:grid-cols-2` 카드 그리드, 썸네일 + 제목 + 날짜
- **이벤트 상세:** `/notice/[id]` 서버 컴포넌트, `generateMetadata` SEO

### 8. Notice 페이지 서버 컴포넌트 전환

- 이전: `"use client"` + `useEffect`로 클라이언트 Supabase 쿼리 → SEO 불리, 초기 빈 화면
- 이후: `createServerSupabaseClient()`로 서버에서 데이터 fetch → HTML에 데이터 포함, SEO 최적화

### 9. `useImageUpload` 커스텀 훅

- `src/hooks/useImageUpload.ts` 신규 — 이미지 업로드 공통 로직 훅
- 5개 Admin 페이지에서 중복 코드 제거

### 10. SEO + 인프라

| 항목 | 내용 |
|------|------|
| sitemap.xml | `src/app/sitemap.ts` 동적 생성 (정적 + 동적 페이지) |
| robots.txt | `/admin/` 차단, sitemap.xml 위치 안내 |
| Vercel Analytics | `<Analytics />` 루트 레이아웃에 추가 |
| 네이버 Search Advisor | 메타태그 소유권 인증 + 사이트맵 제출 |
| 구글 Search Console | 메타태그 소유권 인증 + 사이트맵 제출 |
| 동적 OG 이미지 | `next/og` `ImageResponse` — 포트폴리오/공지 상세 1200x630 자동 생성 |
| 세션 쿠키 보안 | `admin-session` 쿠키에 `Secure` 플래그 추가 |

---

## 코드 품질 점수

| 항목 | 이전 (3.7/5) | 이후 (4.3/5) |
|------|-------------|-------------|
| DRY (코드 중복) | 미흡 | 우수 |
| 컴포넌트 설계 | 보통 | 우수 |
| 에러 처리 | 미흡 | 양호 |
| 보안 | 미흡 | 양호 |
