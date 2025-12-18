# [Troubleshooting] Playwright E2E 테스트 리디렉션 무한 루프 해결기

## 1. 개요
* **프로젝트명**: `cbt_playwright 작성` (대학교 시험용 CBT 솔루션)
* **일시**: 2025년 11월 ~ 12월
* **문제 상황**: Jenkins CI 환경에서 Playwright 테스트(`step6`) 실행 시, 특정 구간에서 `/prelogin` 페이지로 강제 리디렉션되며 테스트가 실패함.

---

## 2. 문제 현상 (Problem)
- **현상**: 좌석 선택 후 인증번호 입력 페이지로 이동하려는 찰나, 의도치 않게 초기 페이지인 `/prelogin`으로 튕겨 나감.
- **환경별 차이**: 로컬 환경에서는 100% 성공하나, Jenkins 환경에서는 간헐적으로 성공하거나 브라우저(Webkit, Firefox 등)에 따라 결과가 달라지는 **Flaky Test** 양상을 보임.

---

## 3. 원인 분석 및 디버깅 과정 (Investigation)

### 3.1 초기 대응 및 가설 설정
1. **단순 지연 처리**: `page.waitForTimeout()`을 추가하여 실행 속도를 늦춰보았으나, 근본적인 해결책이 되지 못함.
2. **로그 추적**: Playwright 로그와 브라우저 콘솔 로그를 대조하며 문제 발생 지점을 특정함.
3. **가설**: 브라우저의 비동기 저장소(IndexedDB)에 데이터가 쓰여지는 속도와 프론트엔드 라우팅 가드(Routing Guard) 로직 간의 **경합 조건(Race Condition)**일 것으로 추측.

### 3.2 결정적 단서 발견
- 선임 개발자와의 협업 및 페어 프로그래밍을 통해 프론트엔드 핵심 로직인 `useCoreContextState.ts`를 집중 분석함.
- **문제의 핵심**: 좌석 선택 정보가 IndexedDB에 저장 완료되기 전에 `useEffect` 내의 인증 체크 로직이 먼저 실행되어 "인증 정보 없음"으로 오판하고 리디렉션을 유발함.

---

## 4. 해결 방법 (Solution)

### 4.1 프론트엔드 코드 리팩토링 (`useCoreContextState.ts`)
- 리디렉션을 유발하는 조건문을 논리적으로 분할함.
- 비동기 데이터 저장 프로세스가 진행 중인 상태(과도기)를 인지할 수 있도록 로직을 개선하여, 데이터가 아직 저장 중일 때는 리디렉션을 보류하도록 수정.

### 4.2 테스트 코드 보강 (`user-exam.spec.ts`)
- 단순히 시간을 기다리는 대신, 핵심 데이터(`student_sit`)가 저장소에 반영되었는지 확인하거나 네트워크 응답이 완료되었는지 체크하는 방식으로 로직을 고도화함.

---

## 5. 결과 및 성과 (Results)
- **빌드 안정화**: 한 달간 지속되던 Jenkins 빌드 오류를 완전히 해결함.
- **크로스 브라우징 성공**: Chromium, Firefox, Webkit 등 모든 브라우저 환경에서 테스트 통과.
- **코드 품질 향상**: 테스트 실패의 원인을 분석하는 과정에서 실제 서비스 소스 코드의 잠재적인 레이스 컨디션 버그를 찾아내어 수정함.

---

## 6. 교훈 및 회고 (Lessons Learned)
1. **관점의 확장**: E2E 테스트의 실패 원인이 반드시 테스트 코드에만 있는 것은 아님. 애플리케이션의 비동기 로직과 브라우저 특성이 복합적으로 작용할 수 있음을 배움.
2. **협업의 가치**: 막혔을 때 동료에게 공식적으로 도움을 요청하고 로그를 함께 분석하는 과정이 문제 해결 속도를 획기적으로 높여줌.
3. **기록의 중요성**: 한 달간의 트러블슈팅 과정을 기록하여 유사한 레이스 컨디션 문제 발생 시 대응할 수 있는 자산을 만듦.

---

인턴십 과정 중 트러블슈팅 기록 (IBST)

useCoreContextState.ts

변경 전
useEffect(() => {
        if(
            needRedirect && 
            !(
                pathname === "/prelogin" ||
                pathname.startsWith("/Result/")
            )
        ) {
            setNeedRedirect(false)
            return navigate('/prelogin', {replace: true})
        }
    }, [needRedirect, pathname]);

변경 후
useEffect(() => {
        if(needRedirect) {
            if(!(
                pathname === "/prelogin" ||
                pathname.startsWith("/Result/")
            )) {
                setNeedRedirect(false)
                return navigate('/prelogin', {replace: true})
            } else {
                setNeedRedirect(false);
            }
        }
    }, [needRedirect, pathname]);
