# rough-see 프로젝트 트러블슈팅 모음

> 출처: rough-see-web 개발 중 발생한 이슈 모음 (day1 + dev-setup-guide)

---

## 1. HTML5 DnD API — 드래그 중 스크롤 차단

**증상:** Pricing 관리 페이지에서 드래그 앤 드롭으로 상품 순서 변경 중 마우스 휠 스크롤이 안 됨.

**원인:** 브라우저가 HTML5 `dragstart` 이벤트 이후 `wheel` 이벤트를 기본 차단함.

**해결:** HTML5 DnD API를 완전히 포기하고 `mousedown` / `mousemove` / `mouseup` pointer-event 기반 커스텀 드래그로 재작성.
- ≡ 핸들을 잡은 상태에서 마우스 휠 스크롤 정상 동작
- 뷰포트 상/하단 근접 시 자동 스크롤 기능도 함께 구현

---

## 2. TipTap v3 — `@tiptap/extension-text-style` import 에러

**증상:** `import TextStyle from "@tiptap/extension-text-style"` 빌드 실패.

**원인:** TipTap v3에서 **default export가 제거됨**. named export만 지원.

**해결:**
```typescript
// ✅ 올바른 import (v3)
import { TextStyle, FontSize } from "@tiptap/extension-text-style";

// ❌ 잘못된 import (v2 방식)
import TextStyle from "@tiptap/extension-text-style";
```

---

## 3. TipTap SSR 하이드레이션 불일치

**증상:** Next.js 서버/클라이언트 간 TipTap 에디터 DOM 구조가 달라 하이드레이션 오류 발생.

**원인:** TipTap이 SSR 환경에서 에디터 초기 DOM을 생성하면서 클라이언트 결과와 달라짐.

**해결:** `useEditor`에 `immediatelyRender: false` 옵션 추가.
```typescript
const editor = useEditor({
  immediatelyRender: false,  // SSR 호환
  // ...
});
```

---

## 4. Next.js App Router — 정적/동적 라우트 충돌 (404)

**증상:** `/portfolio/casual`, `/portfolio/baby` 등 특정 경로에서 404 에러.

**원인:** `casual/`, `baby/`, `wedding/` 정적 폴더와 `[category]/` 동적 폴더를 동시에 만들었을 때, 정적 폴더가 동적 라우트보다 우선권을 가져 `[category]/[id]` 동적 라우트가 작동하지 않음.

**해결:** 정적 폴더 삭제 → 단일 `[category]/page.tsx`로 통합. 내부에서 `slugToCategory`로 유효성 검사 후 유효하지 않으면 `notFound()` 처리.

---

## 5. 포트 충돌 (EADDRINUSE: 3000)

**증상:** `npm run dev` 실행 시 "address already in use :::3000" 에러.

**해결 (Windows):**
```bash
# 3000번 포트 사용 중인 프로세스 찾기
netstat -ano | findstr :3000

# 해당 PID 종료
taskkill /PID <PID> /F
```

---

## 6. 빌드 에러 — 환경 변수 없음

**증상:** `.env.local` 없이 실행 시 런타임 에러.

**원인:** Supabase 클라이언트가 Proxy 패턴으로 지연 초기화되어 빌드 시에는 에러가 나지 않지만, 런타임에 환경 변수 없으면 에러 발생.

**해결:** 반드시 `.env.local` 또는 `.env.development.local` 파일 확인.
- Vercel CLI로 자동 다운로드: `vercel env pull .env.local`

---

## 7. Admin CRUD 권한 에러 (RLS)

**증상:** Admin 페이지에서 데이터 추가/수정/삭제 시 권한 에러.

**원인:** Supabase RLS가 `is_admin()` 함수로 이메일 화이트리스트를 체크함.

**해결:** `supabase/schema.sql`의 `is_admin()` 함수에 해당 관리자 이메일 추가:
```sql
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
  RETURN (
    auth.role() = 'authenticated'
    AND (auth.jwt() ->> 'email') IN ('관리자이메일@example.com')
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## 8. Sentry 빌드 경고 (DSN 없음)

**증상:** 빌드 시 Sentry 관련 경고 메시지.

**원인:** `NEXT_PUBLIC_SENTRY_DSN` 환경 변수가 없는 상태.

**해결:** `NEXT_PUBLIC_SENTRY_DSN`이 없어도 빌드는 성공함. Sentry가 비활성 상태일 뿐 에러가 아님. DSN을 설정하면 프로덕션 환경에서 자동으로 에러 수집 시작.

---

## 9. Vercel 자동 배포 실패 — Git 이메일 불일치

**증상:** GitHub push 후 Vercel 자동 배포가 실패. `No GitHub account was found matching the commit author email address` 에러.

**원인:** 로컬 Git 전역 이메일이 GitHub 계정 이메일과 다름 (공용 PC 환경).

**해결:** 해당 프로젝트 디렉토리에서만 로컬 설정:
```bash
git config --local user.email "GitHub계정이메일"
git config --local user.name "GitHub사용자명"
```
> `--local` 옵션으로 설정하면 전역 설정에는 영향 없음.
