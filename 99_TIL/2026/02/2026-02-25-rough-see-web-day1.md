# Day 1 작업 일지 (2026-02-25)

## 작업 개요
Admin CMS 기능 강화 - About/Pricing 관리 페이지 신규 구현 및 WYSIWYG 에디터 추가

---

## 1. About 페이지 - Supabase 동적 데이터 연동

### 변경 파일
- `src/app/about/page.tsx` — 하드코딩 → Supabase `about` 테이블에서 동적 조회로 변경
- `src/app/admin/about/page.tsx` — **신규 생성**. About 페이지 관리 화면
- `supabase/about.sql` — **신규 생성**. `about` 테이블 DDL + RLS + 초기 데이터

### 구현 내용
- 공개 About 페이지가 DB에서 브랜드 소개 정보를 동적으로 가져옴
- Admin에서 About 내용(텍스트, 이미지 등)을 직접 편집/저장 가능
- DB 데이터 없을 시 기존 하드코딩 fallback 유지

---

## 2. Pricing 관리 페이지 - 카테고리/항목 CRUD + 드래그 앤 드롭

### 변경 파일
- `src/app/admin/pricing/page.tsx` — **신규 생성**. 가격표 관리 전체 페이지
- `src/app/pricing/page.tsx` — 하드코딩 → Supabase `pricing_categories` + `pricing_items` 동적 조회
- `supabase/pricing.sql` — **신규 생성**. 가격표 관련 테이블 DDL

### 구현 내용
- 카테고리(섹션) 및 항목(아이템) CRUD 기능
- **커스텀 pointer-event 기반 드래그 앤 드롭** 구현
  - 기존 HTML5 DnD API 사용 시 브라우저가 스크롤을 차단하는 문제 발생
  - `mousedown` / `mousemove` / `mouseup` 이벤트 기반으로 완전 재작성
  - ≡ 핸들을 잡은 상태에서 마우스 휠 스크롤 정상 동작
  - 뷰포트 상/하단 근접 시 자동 스크롤 기능 포함
- 카테고리 간, 항목 간 순서 변경 가능
- `display_order` 컬럼으로 정렬 순서 DB 반영

---

## 3. 가격표 공통 안내 WYSIWYG 에디터 (TipTap)

### 변경 파일
- `src/components/admin/PricingCommonEditor.tsx` — **신규 생성**. TipTap 에디터 컴포넌트
- `supabase/pricing_common_info.sql` — **신규 생성**. 공통 안내 HTML 저장 테이블
- `src/app/globals.css` — 에디터 및 공개 페이지 렌더링 스타일 추가

### 구현 내용
- 블로그 스타일 WYSIWYG 에디터 (TipTap v3 기반)
  - 굵게(B), 기울임(I), 밑줄(U), 글씨 색상, 글씨 크기 (12px ~ 24px), 정렬 (좌/중/우), 이미지 업로드
- 접이식(collapsible) 카드 UI
- 공개 페이지에서 `dangerouslySetInnerHTML`로 렌더링

### 외부 라이브러리 추가
```
@tiptap/react @tiptap/pm @tiptap/starter-kit
@tiptap/extension-image @tiptap/extension-text-align
@tiptap/extension-color @tiptap/extension-text-style
@tiptap/extension-underline
```

---

## 4. 빌드 중 발생한 이슈 및 해결

| 이슈 | 원인 | 해결 |
|------|------|------|
| HTML5 DnD 스크롤 차단 | 브라우저 기본 동작으로 drag 중 wheel 이벤트 차단 | pointer-event 기반 커스텀 드래그로 완전 재작성 |
| `@tiptap/extension-text-style` import 에러 | TipTap v3에서 default export 제거 | `{ TextStyle, FontSize }` named import로 변경 |
| TipTap SSR 하이드레이션 불일치 | SSR에서 에디터 DOM이 달라 React 하이드레이션 오류 | `useEditor({ immediatelyRender: false })` 설정 |

---

## 5. DB 테이블 현황 (Supabase)

| 테이블 | SQL 파일 | 용도 |
|--------|----------|------|
| `portfolios` | `supabase/init.sql` | 포트폴리오 갤러리 |
| `boards` | `supabase/init.sql` | 공지사항/이벤트 |
| `about` | `supabase/about.sql` | 브랜드 소개 |
| `pricing_categories` | `supabase/pricing.sql` | 가격표 카테고리 |
| `pricing_items` | `supabase/pricing.sql` | 가격표 항목 |
| `pricing_common_info` | `supabase/pricing_common_info.sql` | 가격표 공통 안내 HTML |
