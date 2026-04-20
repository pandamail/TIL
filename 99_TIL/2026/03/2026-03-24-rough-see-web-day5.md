# Day 5 작업 일지 (2026-03-24)

## 작업 개요
Claude Code CLI 설치 / 에러 처리 보강 / 포트폴리오 카테고리 DB화 (하드코딩 제거)

---

## 1. Claude Code CLI 설치

```bash
npm install -g @anthropic-ai/claude-code
# → 2.1.80 설치 완료
```

- 터미널에서 `claude` 명령어 직접 사용 가능
- VSCode 확장과 터미널 Claude Code는 동일 계정(Pro Account) 연결

---

## 2. 에러 처리 보강

### 문제
공개 페이지(서버 컴포넌트) 4개와 Admin 포트폴리오 페이지에서 Supabase 쿼리 실패 시 아무런 처리 없이 페이지 전체가 에러 화면으로 대체되거나 조용히 빈 상태가 됨.

### 수정 파일 (5개)

| 파일 | 수정 내용 |
|------|----------|
| `src/app/page.tsx` | 3개 쿼리 `Promise.all` → `try-catch` 감싸기 |
| `src/app/about/page.tsx` | 단일 쿼리 → `try-catch` 추가 |
| `src/app/pricing/page.tsx` | 3개 분리 쿼리 → `Promise.all` + `try-catch` 통합 |
| `src/app/notice/page.tsx` | 단일 쿼리 → `try-catch` 추가 |
| `src/app/admin/portfolios/page.tsx` | `fetchPortfolios`에 `try-catch` + `loadingList` 상태 + `toast.error` 추가 |

### 정책
- **공개 페이지:** DB 오류 시 크래시 없이 fallback 데이터(빈 배열/기본값)로 렌더링 유지
- **Admin 페이지:** DB 오류 시 `toast.error`로 즉시 사용자에게 알림

---

## 3. 포트폴리오 카테고리 DB화

### 문제
포트폴리오 대분류·소분류가 `src/constants/index.ts`에 하드코딩되어 있어, 새 카테고리 추가 시 개발자가 코드를 직접 수정해야 했음.

```typescript
// 기존 — 코드에 하드코딩
export const categoryMap = {
  "캐주얼 스냅": ["개인 스냅", "커플 스냅", "가족 스냅", "우정 스냅"],
  "베이비 스냅": ["만삭 스냅", "홈스냅", "돌스냅"],
  "웨딩 스냅": ["야외 스냅", "실내 스냅"],
};
```

### 해결 — DB 테이블 신규 생성

`supabase/migration-categories.sql`:

```sql
CREATE TABLE portfolio_categories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE,              -- 대분류명: "캐주얼 스냅"
  slug TEXT NOT NULL UNIQUE,             -- URL slug: "casual"
  label_en TEXT NOT NULL DEFAULT '',     -- 영문명: "Casual Snap"
  description TEXT NOT NULL DEFAULT '',
  subcategories TEXT[] NOT NULL DEFAULT '{}',
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

> Supabase 대시보드 SQL Editor에서 `supabase/migration-categories.sql` 실행 필요.  
> 기존 3개 카테고리(캐주얼/베이비/웨딩)는 시드 데이터로 자동 삽입.

### 수정 파일 (9개)

| 파일 | 수정 내용 |
|------|----------|
| `src/types/index.ts` | `PortfolioCategory` 타입 추가 |
| `src/app/admin/categories/page.tsx` | **신규** — 카테고리 CRUD 관리 페이지 (순서 변경 포함) |
| `src/app/admin/layout.tsx` | 사이드바에 "카테고리 관리" 메뉴 추가 |
| `src/app/admin/page.tsx` | 대시보드에 카테고리 관리 카드 추가 |
| `src/app/admin/portfolios/page.tsx` | 대분류·소분류 드롭다운 → DB에서 동적 로드 |
| `src/app/page.tsx` | `portfolio_categories` 함께 fetch → `MasonryGallery`에 전달 |
| `src/components/MasonryGallery.tsx` | 하드코딩 `mainCategories` 제거, `categories` prop 기반 동적 렌더링 |
| `src/app/portfolio/[category]/page.tsx` | slug 조회 DB 기반으로 변경 |
| `src/app/portfolio/[category]/[id]/page.tsx` | slug 검증 DB 기반으로 변경 |

### 잔여 하드코딩 (향후 개선 예정)

| 파일 | 상수 | 영향 |
|------|------|------|
| `src/components/Header.tsx` | `PORTFOLIO_CATEGORIES` | 헤더 드롭다운에 새 카테고리 미반영 |
| `src/components/PortfolioAlbum.tsx` | `categoryToSlug` | 앨범 링크 생성 시 새 카테고리 slug 미인식 (fallback: "casual") |
| `src/app/sitemap.ts` | `categoryToSlug` | 사이트맵에 새 카테고리 URL 미포함 |

---

## 4. 빌드 결과

```
✓ Compiled successfully
✓ Generating static pages (17/17)
→ 빌드 에러 없음
```
