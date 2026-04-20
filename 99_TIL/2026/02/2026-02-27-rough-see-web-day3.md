# Day 3 작업 일지 (2026-02-27)

## 작업 개요
**오전:** 포트폴리오 URL 구조 재설계, 헤더 드롭다운 메뉴, 카테고리별 앨범 페이지, Admin 가격표 대규모 개선  
**오후:** RLS 보안 강화, 이미지 업로드 최적화, 접근성(a11y), Sentry 연동, Supabase 환경 분리  
**저녁:** 평일/주말 가격 분리, 촬영 일정 캘린더, 포트폴리오 앨범 레이아웃 개선

---

## 오전 작업

### 1. 포트폴리오 URL 구조 재설계

- **기존:** `/portfolio/[id]`
- **변경:** `/portfolio/[category]/[id]` (예: `/portfolio/casual/abc123`)

```typescript
// src/constants/index.ts
const PORTFOLIO_CATEGORIES = ['casual', 'baby', 'wedding'];
const categoryToSlug = { '캐주얼 스냅': 'casual', ... };
const slugToCategory = { 'casual': '캐주얼 스냅', ... };
```

**해결한 이슈:** 정적 폴더(`casual/`, `baby/`)와 동적 `[category]/` 폴더를 동시에 만들면 정적 폴더가 우선하여 동적 라우트 404 에러 발생 → 정적 폴더 삭제 + 단일 `[category]/page.tsx`로 통합.

### 2. 헤더 Portfolio 드롭다운 메뉴

- **데스크톱:** 마우스 hover 시 3개 카테고리 드롭다운 (motion AnimatePresence)
- **모바일:** 터치 시 아코디언 방식으로 서브메뉴 펼침/접기

### 3. 카테고리별 앨범 페이지

- `src/components/PortfolioAlbum.tsx` 신규 — CSS columns 기반 메이슨리 레이아웃 (1열→2열→3열)
- hover 시 제목 표시 + 이미지 확대 애니메이션

### 4. 포트폴리오 상세 디자이너 감각 리디자인

- **사진 배치 패턴** (반복): full → duo → full → aside → trio
- `aspect-*` + `object-cover` 제거 → 사진 자연 비율 유지 (`w-full h-auto`)
- 하단 CTA: "가격표 보러가기" + "카카오톡으로 문의하기"

### 5. Admin 가격표 대규모 개선

- 카테고리 확장/축소 토글 + 다중 상품 동시 편집 (`editingItems: Record<string, Form>`)
- 카테고리/상품 복제 기능 (이름에 "(복사)" 접미사)
- 체크박스 + 일괄 태그 변경 (`Promise.all` batch UPDATE)

---

## 오후 작업

### 6. RLS 보안 강화

```sql
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
  RETURN (
    auth.role() = 'authenticated'
    AND (auth.jwt() ->> 'email') IN ('01freesoul01@gmail.com')
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

- 기존: `authenticated`만 체크 → 로그인한 아무나 데이터 수정 가능
- 변경: 이메일 화이트리스트 방식, 8개 테이블 + Storage 버킷 전체 정책 교체

### 7. 이미지 업로드 최적화

- `src/utils/compressImage.ts` 신규 — Canvas API 기반 압축
  - 최대 2048×2048px 리사이즈 + WebP 변환 (85% 품질)
  - 50MB 초과 시 업로드 차단
  - GIF 및 500KB 이하 스킵

### 8. 접근성(a11y) 개선

| 항목 | 상세 |
|------|------|
| Skip to content | Tab으로 본문 바로 이동 (`sr-only focus:not-sr-only`) |
| Dialog ARIA | 팝업에 `role="dialog"`, `aria-modal="true"`, `aria-label` |
| 키보드 네비게이션 | 헤더 드롭다운 onFocus/onBlur + `aria-expanded` |
| 아코디언 ARIA | `aria-expanded`, `aria-controls`, `role="region"` |

### 9. Sentry 에러 모니터링

```
sentry.client.config.ts / sentry.server.config.ts / sentry.edge.config.ts
```

- `@sentry/nextjs` v10, 3개 환경(client/server/edge) 각각 초기화
- 프로덕션에서만 활성화 (`process.env.NODE_ENV === "production"`)
- 성능 샘플링 10%, 에러 세션 리플레이 100%

**해결한 이슈:** `disableServerWebpackPlugin` 타입 에러 → Sentry v10 API 변경으로 `sourcemaps: { disable: true }` 사용.

### 10. Supabase 환경 분리 (dev/prod)

- `rough-see-dev` Supabase 프로젝트 신규 생성 (Seoul 리전)
- 로컬: `.env.local` → dev DB
- Vercel Production: 기존 운영 DB 유지
- `docs/supabase-env-separation.md` 가이드 문서 생성

---

## 저녁 작업

### 11. 가격표 평일/주말 가격 분리

```sql
ALTER TABLE pricing_items ADD COLUMN price_weekend TEXT NOT NULL DEFAULT '';
ALTER TABLE pricing_items ADD COLUMN sale_price_weekend TEXT NOT NULL DEFAULT '';
```

- Admin: 미니 테이블 레이아웃 (평일/주말 행, 정가/할인가 열)
- 공개 페이지: `price_weekend`가 있으면 1개 상품에서 평일/주말 2행 표시
- 주말 가격 없으면 기존 1행 표시 (하위 호환)

### 12. 촬영 가능일 캘린더

- `src/app/schedule/page.tsx` 신규 — Google Calendar API 연동
- `src/app/api/calendar/route.ts` 신규 — 서버 사이드 프록시
- 환경 변수: `NEXT_PUBLIC_GOOGLE_CALENDAR_ID`, `GOOGLE_CALENDAR_API_KEY`

### 13. 포트폴리오 앨범 레이아웃 개선

- 게시글 수에 따라 CSS columns 자동 변경 (1~2개: `columns-1 sm:columns-2`, 6개+: `columns-2 sm:columns-3`)
- 카테고리 헤더 제거 → 사진만 표시

---

## 설치된 패키지

| 패키지 | 용도 |
|--------|------|
| `@vercel/speed-insights` | 실시간 Web Vitals 모니터링 |
| `@sentry/nextjs` | 프로덕션 에러 모니터링 |
