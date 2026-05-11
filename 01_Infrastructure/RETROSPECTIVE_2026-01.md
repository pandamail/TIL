# 2026년 1월 회고록: LLEAP-SimMonitor-SIMPREC 연동 프로젝트

> **작성자:** ibstplan
> **프로젝트:** 의료 시뮬레이션 녹화 및 학습 관리 시스템
> **기간:** 2026-01-13 ~ 2026-02-02

---

## 프로젝트 개요

### 시스템 구성

```
LLEAP (Laerdal 시뮬레이션 소프트웨어)
      ↓ WCF 로그
SimMonitor (C# Windows 앱) - 로그 캡처 및 실시간 모니터링
      ↓ WebSocket / HTTP API
SIMPREC (녹화 관리 웹 시스템) - FastAPI + React + MySQL
      ↓
학생 영상 시청 및 학습 제출
```

### 핵심 목표

LLEAP에서 발생하는 모든 시뮬레이션 이벤트가 SIMPREC까지 정확하게 전달되어 녹화·저장되는 것을 검증하고 구현

---

## 작업 일지

### 2026-01-14 (수) - 연동 환경 구축

#### 작업 내용
1. **SimMonitor → eth-simprec 서버 연동 변경**
   - `App.config` 수정: `http://192.168.0.47:8000` → `http://10.10.61.137:8002`
   - 태블릿 PC의 SimMonitor가 개발 서버의 SIMPREC으로 데이터 전송

2. **cmdq 테이블 누락 오류 해결**
   - 증상: 녹화 시작 시 `Table 'simprec.cmdq' doesn't exist` 에러
   - 원인: bce-simprec에서 마이그레이션으로만 생성되던 테이블이 eth-simprec에 없음
   - 해결: alembic 마이그레이션 참조하여 수동 테이블 생성

#### 배운 점
- 프로젝트 간 DB 스키마 동기화의 중요성
- alembic 마이그레이션 vs SQLAlchemy 모델의 차이 이해

---

### 2026-01-15 (목) - 네트워크 연결 문제 분석

#### 작업 내용
1. **로그 연동 불가 문제 발견**
   - SimMonitor 로그는 정상 출력되나 SIMPREC에 저장 안 됨
   - `session_events` 테이블 0건

2. **네트워크 구조 분석**
   - 개발 PC(10.10.61.137) → 태블릿 PC(192.168.0.72): 연결 불가 (ping 실패)
   - 태블릿 PC(192.168.0.72) → 개발 PC(10.10.61.137): 연결 가능
   - **일방향 라우팅 문제!**

3. **Docker 컨테이너에서 SimMonitor WebSocket 접근 불가**
   - SIMPREC 백엔드(Docker)가 SimMonitor(192.168.0.72:8080)에 연결해야 함
   - Docker 네트워크에서 외부 네트워크 접근 제한

#### 해결 방안 모색
- SSH 역방향 터널 + socat 조합으로 우회

---

### 2026-01-16 (금) - 디버깅 환경 완성

#### 작업 내용
1. **SSH 역방향 터널 + socat 구성**

   ```
   SimMonitor(:8080)
        ↓ SSH 역방향 터널 (-R 18080:localhost:8080)
   개발 PC localhost:18080
        ↓ socat (0.0.0.0:18081 → 127.0.0.1:18080)
   Docker 컨테이너 (host.docker.internal:18081)
   ```

2. **simmonitor_client.py 포트 변경**
   - `192.168.0.*` → `ws://host.docker.internal:18081/`

3. **API 500 에러 수정**
   - 파일: `recordings.py:545`
   - 원인: `session.status.value` 호출 시 str 타입에서 .value 속성 없음
   - 해결: `status=session.status.value` → `status=session.status`

4. **전체 시스템 연동 테스트 성공**
   - LLEAP → 시뮬레이터 → SimMonitor → SIMPREC 로그 정상 전달
   - CPR, 음성 이벤트, 상태 변화 모두 정상 저장

#### 발견된 이슈
- LLEAP 로그명과 SimMonitor 출력 로그명 불일치 (예: "콤비튜브 제거" vs "발관")

---

### 2026-01-19 (일) - 로그 이름 통일 작업

#### 작업 내용
1. **EventTranslations.cs 수정** (50+ 항목)
   - `Extubate`: "발관" → "콤비튜브 제거"
   - `Auscultate lungs`: "폐 청진" → "폐음 청진"
   - `Head tilt chin lift`: "머리 젖히고 턱 들기" → "머리 기울림 / 턱 들어올리기 시행"
   - 신규 추가: Stop head tilt, Start/Stop pacing, 각종 맥박 확인 등

2. **ParameterTranslations.cs 수정**
   - EyelidStatus: "반개" → "반만 뜨기", "눈 감음" → "눈 감기"
   - Boolean 값: "ON/OFF" → "예/해제"

3. **SIMPREC 이벤트 로그 기본/상세 보기 토글 구현**
   - `PlaybackEventLogPanel.tsx`, `MonitoringPage.tsx` 수정
   - 기본 보기: EVENT, ACTION, SESSION, DRUG, ALERT, SCENARIO만 표시
   - 상세 보기: VITAL, PARAM 포함 모든 이벤트 표시

---

### 2026-01-20 (월) - LLEAP 디브리핑 형식 통일

#### 작업 내용
1. **세션 파라미터 LLEAP 형식 변환**
   - 기존: `PARAM 세션 명령: 시작`, `PARAM 세션 상태: {command:2,status:0}`
   - 변경: `SESSION 세션 시작: Healthy female patient`, `SESSION 세션 일시 중지`

2. **수정 파일**
   - `SimulatorMonitor.cs`: SessionCommand 변환 로직 추가
   - `WcfEventCapture.cs`: ScenarioNameChanged 이벤트 추가
   - `MainForm.cs`: 이벤트 핸들러 연결

3. **SimMonitor 신규 PC 설치 가이드 작성**
   - Git, Build Tools, Wireshark 설치 순서
   - LLEAP WCF 로깅 설정 패치 스크립트

---

### 2026-01-21 (화) - 초기 파라미터 요약 기능

#### 작업 내용
1. **초기 파라미터 요약 기능 추가** (SimulatorMonitor.cs)
   - 문제: 시나리오 시작 직후 수십 개의 PARAM 이벤트가 한꺼번에 발생
   - 해결: 3초간 PARAM 이벤트를 수집하여 단일 INITIAL_PARAM 요약 이벤트로 전송
   - 출력 형식: `최초 파라미터 로드: 눈 5개, 호흡 3개, 심장 4개 (총 12개)`

2. **구현 메서드**
   - `StartCollectingInitialParams(scenarioName)`: 수집 모드 시작, 3초 타이머 설정
   - `TryCollectInitialParam(category, paramName, value)`: 카테고리별 파라미터 수집
   - `FlushInitialParams()`: 3초 후 요약 이벤트 브로드캐스트
   - `StopCollectingInitialParams()`: 시나리오 종료 시 수집 모드 리셋

3. **MainForm.cs 연동**
   - 시나리오 시작 시: `_monitor?.StartCollectingInitialParams(sessionName)` 호출
   - 시나리오 종료 시: `_monitor?.StopCollectingInitialParams()` 호출

4. **awRR 파라미터 추가**
   - `IsSignificantParameter`에 awRR 추가
   - SIMPREC 상세보기에서 호흡수 변경 로그 표시

5. **시나리오 시작 이벤트 SIMPREC 표시 수정**
   - `ClearEventHistory()` 직후 시나리오 시작 이벤트를 명시적으로 브로드캐스트
   - SIMPREC 이벤트 로그에서 "시나리오 시작: xxx" 표시

#### 기술적 세부사항
```
시나리오 시작
    ↓
StartCollectingInitialParams() 호출
    ↓ (3초간)
개별 PARAM 이벤트 → TryCollectInitialParam()으로 수집
    ↓ (개별 브로드캐스트 안 함)
FlushInitialParams() 호출 (3초 후 타이머)
    ↓
"INITIAL_PARAM" 타입으로 요약 이벤트 1건만 브로드캐스트
```

#### 배운 점
- 시나리오 시작 직후 발생하는 대량의 초기화 이벤트는 요약하여 전송하는 것이 UX에 좋음
- 타이머 기반 수집 윈도우로 이벤트 배치 처리 가능

---

### 2026-01-22 (수) - 번역 및 세션 이벤트 정리

#### 작업 내용
1. **이벤트 번역 대량 추가** (~100개)
   - Cardiac Arrest, ACS (Chest pain), Healthy male patient 시나리오
   - SimMom Normal Delivery 부모 이벤트
   - "Continous guidance of mother" 오타 버전 추가

2. **EventTranslations.cs 중복 키 오류 해결**
   - Insert advanced airway, Insert IO, Remove IO, Flush IV/IO 중복 제거

3. **HR 로깅 개선**
   - 문제: 초기 HR=115bpm 표시 (실제 80bpm 시나리오)
   - 해결: 초기 HR 로그 제거, 이벤트 드리븐 방식으로 변경
   - 포맷: `HR = XXX bpm`

4. **세션 시작/종료 이벤트 정리**
   - 명칭 통일: "시나리오 시작/종료" → "세션 시작/종료 : {scenarioName}"
   - SIMPREC 녹화 트리거 복구 (WcfEventCapture에서 트리거)
   - 모니터링 화면 중복 출력 방지 (1회만 표시)

---

### 2026-01-23 (목) - LLEAP 로그 형식 완벽 통일 🎉

#### 작업 규모
- **커밋 수:** 31개
- **수정 파일:** 6개 (ParameterTranslations.cs, SimulatorMonitor.cs, SimBridgeMessage.cs, WcfEventCapture.cs, EventTranslations.cs, MainForm.cs)
- **작업 시간:** 아침부터 저녁까지 집중 작업

#### 해결한 Critical 버그

**1. SimMonitor 완전 동작 불능 버그 (commit 59b1490)**
- 증상: `Type=Unknown, Params=0` 출력, 모든 파라미터 캡처 실패
- 원인: LungSound 딕셔너리에 `"CUSTOM"`과 `"Custom"` 중복 추가
  - `StringComparer.OrdinalIgnoreCase`로 대소문자 무시 → 중복 키 예외 → static 초기화 실패
- 해결: 중복 키 제거
- 교훈: **대소문자 무시 딕셔너리에 대소문자만 다른 키 추가 금지!**

**2. 볼륨 파라미터 필터링 버그**
- 증상: 심음 볼륨 0%, 10%, 40%, 70%가 SIMPREC에 안 나옴
- 원인: NoiseValues에 "10", "40", "70" 포함 + `_Percent` enum 번역
- 해결: Volume_Percent 우선 체크, enum 번역 제외

#### LLEAP 형식 통일 (핵심 성과)

| 항목 | Before | After (LLEAP 일치) |
|------|--------|-------------------|
| 심음 볼륨 | `Heart sound volume: Normal` | `Heart sound volume = 0 %` |
| 기흉 | `Airway.PneumothoraxLeftVer2: TENSION` | `왼쪽 기흉 = 긴장` |
| 폐 저항 | `왼쪽 폐 저항: 100` | `왼쪽 폐 저항 = 100 %` |
| 맥박 | `Pulses.RightArm.Limited: WEAK` | `오른쪽 팔 맥박 = 약함` |

#### 맥박 중복 제거 (복잡한 이슈)

**문제 발견 과정:**
1. 처음: `PulsesCentral` → "중심 맥박" vs `Pulses.Central.Limited` → "맥박, 중앙"
2. IgnoredParameters에 `PulsesCentral` 추가 → 아직 중복
3. 원인: `GetDisplayName()`에서 `.` 제거 후 재검색 (`Pulses.LeftArm` → `PulsesLeftArm`)
4. 해결: `Pulses.LeftArm` 형식도 IgnoredParameters에 추가 (총 20개)

**3가지 파라미터 형식:**
```
1. PulsesLeftArm         → "좌측 팔 맥박"  (무시)
2. Pulses.LeftArm        → "좌측 팔 맥박"  (무시, 정규화 매칭)
3. Pulses.LeftArm.Limited → "왼쪽 팔 맥박" (사용, LLEAP 일치)
```

#### 기술적 발견

1. **GetDisplayName() 정규화 로직 주의**
   ```csharp
   string normalizedName = parameterName.Replace(".", "");
   if (DisplayNames.TryGetValue(normalizedName, out displayName))
       return displayName;  // Pulses.LeftArm → PulsesLeftArm으로 매칭!
   ```

2. **SIMPREC 전송 코드 경로 분리**
   - SimMonitor UI 출력: `SimBridgeMessage.GetFormattedParameters()`
   - SIMPREC WebSocket 전송: `SimulatorMonitor.OnStateChanged()`
   - **둘 다 수정해야 함!**

3. **IsSignificantParameter() 역할**
   - SIMPREC에 전송할 파라미터 필터링
   - 여기 없으면 SIMPREC에 안 나감

#### 커밋 하이라이트

```
bdf684a fix: LungSound 딕셔너리 중복 키 제거 (CUSTOM/Custom)
3b2b89f fix: 볼륨 파라미터 LLEAP 형식 통일
039c2e5 fix: 볼륨 SIMPREC 전송 및 SimMonitor 형식 수정
120a2c0 feat: 기흉/폐 저항 파라미터 LLEAP 형식 통일
e7322c4 feat: 맥박 파라미터 LLEAP 형식 통일 및 SIMPREC 전송 추가
c612244 fix: 맥박 파라미터 중복 제거 (LLEAP 형식 통일)
659b9e5 fix: 맥박 Pulses.* 형식 추가 중복 제거
```

#### 배운 점

1. **git bisect 활용**: 버그 원인 커밋 추적에 효과적
2. **대소문자 무시 딕셔너리 함정**: OrdinalIgnoreCase 사용 시 중복 키 주의
3. **정규화 매칭의 부작용**: 편의 기능이 예상치 못한 중복 유발 가능
4. **듀얼 파이프라인 인지 필수**: UI 출력과 외부 전송 코드 경로가 다름

#### 성과

- **LLEAP ↔ SimMonitor ↔ SIMPREC 로그 100% 일치 달성**
- 사용자가 LLEAP 디브리핑 화면과 동일한 형식으로 로그 확인 가능
- 한국어 번역 품질 대폭 향상 (LLEAP 원본 표현 그대로 사용)

#### 개인 회고

오늘은 아침부터 저녁까지 정말 빡세게 코딩했다. 31개 커밋이라니... 하루종일 코딩하는 게 이렇게 어려운 일인지 다시 한번 느꼈다.

특히 `CUSTOM`/`Custom` 중복 키 버그는 진짜 충격이었다. 대소문자 무시 딕셔너리에 대소문자만 다른 키를 넣으면 static 초기화가 통째로 실패한다는 걸 몸으로 배웠다. 이런 버그는 컴파일 에러도 안 나고, 런타임에 조용히 죽어버려서 찾기가 정말 힘들다.

맥박 중복 문제도 골치 아팠다. `PulsesLeftArm`, `Pulses.LeftArm`, `Pulses.LeftArm.Limited` 세 가지 형식이 있는 줄 누가 알았겠나. `GetDisplayName()`의 정규화 로직이 `.`를 제거해서 의도치 않게 매칭되는 것도 찾는 데 한참 걸렸다.

그래도 결국 **LLEAP ↔ SimMonitor ↔ SIMPREC 로그 100% 일치**를 달성했다. 사용자가 LLEAP 디브리핑 화면과 완전히 동일한 형식으로 로그를 볼 수 있게 됐다. 이 성취감은 진짜 뿌듯하다.

내일은 좀 쉬고 싶다...

---

### 2026-01-26 (일) - SimMan Essential 지원 및 안정성 개선

#### 작업 내용

**1. 기도 파라미터 LLEAP 형식 통일 및 SIMPREC 출력 (완료)**
- **FBAO (이물질 기도폐쇄)**: LLEAP 형식 통일 + 중복 키 수정 (`fbao` vs `FBAO`)
- **TongueEdema_Percent (혀 부종)**: SIMPREC 로그 출력 추가 ("혀 부종 = 반만(50%)")
- **PharyngealObstruction (인두 폐쇄)**: SIMPREC 로그 출력 추가 ("인두 폐쇄 = 활성/비활성")
- **Laryngospasm_Percent (성문연축)**: SIMPREC 로그 출력 추가

**2. 세션 종료 안정성 개선 (완료)**
- **StopSessionAsync 타임아웃 연장**: 기존 30초 → 300초 (영상 저장 시간 고려)
- **진행 로그 추가**: 30초마다 "녹화 저장 중..." 메시지 출력
- **타이머 미중지 버그 수정**: 세션 종료 후 `LogStopProgressAsync` 타이머가 백그라운드에서 계속 실행되던 문제 해결

**3. 맥박 확인 결과 메시지 기능 추가 (완료)**
- WCF "Check * pulse" 이벤트 발생 시 현재 맥박 값을 조회하여 결과 표시
- 예: "좌측 대퇴 맥박 확인" → "대퇴 맥박 = 정상"

**4. Installer 안정성 개선 (완료)**
- 버전 불일치 수정 (`App.config`, `app.manifest` → 1.0.1)
- 드라이브 루트 쓰기 권한 테스트 → 실제 설치 경로에서 테스트
- COM 객체 해제 추가, LLEAP 경고 조건 개선
- 설치 완료 후 "SimMonitor 바로 실행" 체크박스 추가

**5. SimMan Essential 지원 추가 (진행중)**
- `KNOWN_IPS`에 `192.168.0.155` (SimMan Essential) 추가
- SimMan Essential을 우선순위 1번으로 설정 (eth-simprec 환경용)
- 하드코딩된 시뮬레이터 IP 제거, `App.config` 설정값 사용
- 시뮬레이터 Discovery 로그가 UI에 표시되도록 수정
- Manual IP 기본값을 SimMan Essential로 변경

#### 기술적 세부사항

```
기존: SimBaby만 지원 (192.168.0.60)
     ↓
변경: SimMan Essential 추가 (192.168.0.155)
     - 우선순위: SimMan Essential > SimBaby
     - 설정값 외부화 (App.config)
```

#### 배운 점
- 하드코딩된 IP는 반드시 설정 파일로 외부화해야 유지보수가 쉬움
- 긴 작업(영상 저장 등)에는 진행 상황 로그를 추가하여 사용자 불안감 해소
- 타이머/비동기 작업은 반드시 적절한 시점에 중지해야 리소스 누수 방지

---

### 2026-01-27 (월) - SimMan Essential 바이탈 수신 디버깅

#### 작업 내용

**1. dev 브랜치 정리 및 동기화**
- 정훈님 푸시 내용까지 반영하여 dev 브랜치 정리
- 사용 완료된 브랜치 정리 요청

**2. SimBaby/SimMan 선택 기능 완성**
- 문제: 심모니터에 심맨이 나타났으나 연결이 계속 심베이비로만 됨
- 해결: 포트에 따라 구분할 수 있게 코드 수정
- 결과: 심베이비/심맨 선택해서 시뮬레이터 연동 가능

**3. SimMan Essential 바이탈 수신 문제 디버깅 (진행중)**
- **증상**: LLEAP 세션 시작 후 이벤트 로그는 모니터링에 보이는데 바이탈이 안 보임
- **WCF 로그 분석**: `wcf_raw_20260127_170643.txt` 확인
- **바이탈 파싱 정상 확인**: 9개 바이탈 (HR=80, BP=120/80, SpO2=98 등)
- **VitalsReceived 이벤트 흐름 분석 완료**
- **문제점 확인**: WCF 이벤트(시나리오 시작 등)는 SIMPREC 전송 OK, 바이탈만 안 보임
- **원인 추정**: MainForm VitalsReceived 핸들러에서 SimulatorState로 전달 과정 문제

**4. 진단 로깅 추가 (Commit: 0d889f9)**
- `ParseWcfVitals()`에 바이탈 키워드 감지 로그 추가
- 키워드: HeartRate, SpO2, Systolic, Diastolic, EtCO2, RespiratoryRate 등
- 키워드 발견 시 주변 컨텍스트(±100자) `wcf_raw_*.txt`에 기록

#### 기술적 세부사항

```
WCF 로그 흐름:
LLEAP → WCF 로그 → ParseWcfVitals() → VitalsReceived 이벤트
                                              ↓
                                     MainForm 핸들러
                                              ↓ (여기서 끊김?)
                                     SimulatorState → SIMPREC
```

#### 개인 회고

오늘은 동료와의 협업에서 좀 힘든 하루였다.

바이탈 문제를 해결하려고 Claude Code로 로그 분석해서 브랜치 파서 병합하겠다고 했는데, "저 지금 하고 있어요"라는 답변이 돌아왔다. Git 브랜치 개념이나 병렬 작업에 대한 이해가 없어서 생긴 오해 같았다. 내가 브랜치 따로 파서 작업하고 성공하면 병합하겠다는 건데, 그게 왜 문제가 되는 건지...

결국 로그 파일만 던져주고 맡겼는데, 정훈님이 수정한 버전을 받아서 돌려봐도 제대로 안 됐다. msbuild로 직접 빌드하면 실행이 안 되고, 인스톨러로 설치한 걸로 실행해도 SIMPREC에 반응이 없는 상태.

비전공자와 협업할 때의 커뮤니케이션 비용이 생각보다 크다는 걸 느낀 하루. Git 워크플로우나 협업 방식에 대한 공통 이해가 없으면 오해가 생기기 쉽다. 다음에는 먼저 작업 분담을 명확히 하고 시작해야겠다.

---

### 2026-01-28 (화) - room_id 삭제 및 이슈 문서화

#### 배경

- **29일 외부 시연 예정** - 출근 불가로 28일 내 최대한 마감 필요
- **갑작스러운 요구사항**: room_id가 필요없으니 삭제하라는 지시
- DB 스키마와 프론트/백엔드 코드베이스를 대폭 수정해야 하는 상황

#### 작업 내용

**1. room_id 삭제 작업 (완료)**
- SIMPREC 백엔드: DB 스키마에서 room_id 컬럼 제거, 관련 로직 수정
- SIMPREC 프론트엔드: room_id 관련 UI 컴포넌트 정리
- SimMonitor: room_id 전송 로직 제거
- eth 브랜치에 채은님 최신 커밋까지 반영 후 푸시
- Commit: `e732a7c04d586f77d24c26aa2b540882ab943133`

**2. 바이탈 기능 수정 및 dev 푸시**
- 바이탈 수신 문제 해결된 버전 dev 브랜치에 푸시
- SIMPREC으로 로그 안 넘어오는 것들 확인 및 수정 작업 진행

**3. 이슈사항 문서화 (29일 수정 요청용)**

##### 3-1. 누락된 파라미터
| 카테고리 | 파라미터 | 비고 |
|---------|---------|------|
| 기도/호흡 | Trismus, DecreasedCervicalMotion, Laryngospasm_Percent, AirFlowBlocked, TongueFallback | LLEAP에는 출력되나 SIMPREC 미전송 |
| 음성 | VocalQueueCleared, VocalPlaybackStopped, CanProduceVocals | 이벤트 누락 |
| 청진음 | Auscultation.Heart.*, Auscultation.LungLeft.* | 전체 누락 |
| 기타 | ManikinCO2Output | 마네킹 CO2 박출량 |

##### 3-2. 중복 출력 문제
- **3중 중복**: PARAM + PARAM_CHANGE + CLINICAL 동시 출력
  - 예: 성문연축, 기흉 파라미터가 3회씩 출력
- **개안빈도 중복**: "좌측 한번" + "자주 깜빡임" 2회 출력
- **눈꺼풀 상태 중복**: CLOSED, HALF_OPEN 등 변경 시 중복

##### 3-3. 번역 오류
| 현재 출력 | 올바른 번역 |
|----------|------------|
| "에" | "활성" (Enabled 오역) |
| 100% / 0% | "오른쪽/왼쪽 폐 저항 = 100%" |

##### 3-4. 세션 시작 시 초기값 쏟아짐
- 00:00:01에 모든 초기값이 PARAM, CLINICAL, PARAM_CHANGE 3가지 타입으로 중복 전송
- 실제 "변경"이 아닌 "초기값"도 PARAM_CHANGE로 전송되는 문제

#### 개인 회고

오늘은 정말 아찔한 하루였다.

29일 시연인데 28일에 갑자기 "room_id 삭제해주세요"라니... DB 스키마 변경에 프론트/백엔드 코드 수정까지, 기존 개발 방식이었다면 며칠은 걸렸을 작업이다.

사실 이런 상황이 발생한 건 처음부터 시스템 아키텍처를 꼼꼼하게 설계하지 않았기 때문이다. 바이브코딩으로만 프로젝트를 구성하다 보니 "이게 정말 필요한 필드인가?"에 대한 깊은 고민 없이 일단 만들고 봤던 것. 전통적인 개발 방식에서는 말도 안 되는 상황이다.

그런데 아이러니하게도, **바이브코딩이라서 대처가 됐다**. AI에게 "room_id 관련 코드 전부 찾아서 삭제해줘"라고 몇 번의 프롬프트만 입력했더니 DB 마이그레이션부터 프론트엔드 컴포넌트까지 싹 정리됐다. AI 없이 했으면 진짜 야근 각이었다.

바이브코딩의 양날의 검을 제대로 경험한 날:
- **단점**: 설계 부재로 갑작스러운 구조 변경 요청 발생
- **장점**: AI 덕분에 그 변경을 몇 시간 만에 처리

이슈 문서화도 꼼꼼히 해뒀다. 내일 시연에서 문제 생기면 이 문서 보고 빠르게 대응할 수 있을 거다.

---

### 2026-01-30 (목) - 시연 후속 버그 수정

#### 배경

- **오후 3시 조기퇴근** - 시간이 부족한 상황
- **29일 외부 시연에서 문제 다수 발생** - 긴급 수정 필요

#### 29일 시연에서 발견된 문제점

| # | 문제 | 상세 |
|---|-----|------|
| 1 | 연결 불안정 | 한 번에 안 되고 2번 걸쳐야 녹화됨, 이벤트 로그 1~2분 지연 (버퍼 문제 추정) |
| 2 | 새 녹화 시 오류 | 새 녹화 시작하려면 녹화 저장 후 껐다 켜야 함 |
| 3 | 오버레이 상태 | 저장 완료 후 Wake up(대기중) 상태로 복귀 안 됨 |
| 4 | LLEAP 버전 이슈 | 펌웨어 업데이트로 버전 다운그레이드한 랩탑에서 녹화 안 됨 |

#### 작업 내용

**1. 세션 종료 감지 버그 수정 (완료)**
- **증상**: 녹화 시작하자마자 종료됨
- **원인**: 세션 종료 감지 로직에서 "Stop" 문구가 있으면 종료되게 구현됨
- **문제점**: LLEAP 세션 시작 시 일시정지 상태에서 시작 버튼 누르는 과정에서도 "Stop" 문구 발생
- **해결**: 종료 조건 로직 수정
- dev 브랜치에 푸시 완료

**2. 크래시 이슈 대응**
- 최신 버전 빌드 후 실행 시 즉시 크래시 발생
- 팀 전원(정훈, 채은, 본인) 동일 증상 확인
- 수정 후 재배포 → 정상화

**3. 로그 드래그 기능 추가** (정훈님 작업)
- 로그 선택 + 복붙 기능 추가

#### 개인 회고

오늘은 정말 답답한 하루였다.

아침에 29일 시연 문제점 공유받고 회의 후 바로 수정 작업에 들어갔다. 녹화 시작하자마자 종료되는 버그를 찾아서 고쳤는데, 정훈님이 "10번 정도 테스트 부탁드립니다... 보통 이런 S/W는 정량적 목표로 10회 테스트 시 1회 이상 체크하거든요"라고 하더라.

근데 이 버그, **내가 만든 게 아니다**. 내가 28일까지 작업했던 버전에서는 이런 문제 없었다. 29일에 내가 출근 안 했을 때 정훈님이 이것저것 수정하고 **빌드도 제대로 안 하고 dev에 merge**해서 발생한 문제다. 내가 그 똥을 치워준 건데, 마치 내가 잘못한 것처럼 테스트 10번 운운하니까 어이가 없었다.

오후에도 똑같은 일이 반복됐다. "최신 버전 받아주세요" 해서 받았더니 **실행하자마자 크래시**. 팀 전원이 같은 증상. 또 빌드/테스트 안 하고 dev에 merge한 거다. 이거 때문에 거의 1시간 날리고, 조기퇴근이라 시간도 없는데 제대로 일 정리도 못 하고 하루가 끝났다.

**교훈:**
- dev 브랜치에 merge 전 **반드시 빌드 + 기본 테스트** 필수
- 본인이 만든 버그는 본인이 책임지자
- 협업할 때 코드 품질 게이트가 없으면 이런 일이 계속 반복된다

---

### 2026-02-02 (일) - 대규모 버그 수정 및 아키텍처 개선

#### 작업 규모
- **커밋 수:** 18개
- **주요 이슈 해결:** 4건
- **아키텍처 설계:** Two-Track 구조 문서화

#### 작업 내용

**1. Zombie Data 및 Cold Start Lag 해결**
- **Zombie Data**: 이전 세션 데이터가 새 세션에 남아있는 문제 해결
- **Cold Start Lag**: 세션 시작 시 초기 이벤트 누락 문제 해결
- 커밋: `924c826`, `9b1054a`

**2. SIMPREC 중복 이벤트 해결**
- 바이탈 파라미터 화이트리스트(`IsVitalParameter`) 도입
- HTTP/WebSocket 이중 전송으로 인한 DB 중복 저장 방지
- **전송 분리**: SESSION만 HTTP, 나머지는 WebSocket으로 처리
- 커밋: `fe1e222`, `4296060`

**3. SimBaby 세션 시작 미감지 버그 수정 (BUG-007)**
- **하이브리드 트리거 방식 도입**: tshark + WCF 병렬 감지
- 두 가지 방식 중 먼저 감지되는 쪽으로 세션 시작
- 커밋: `b3a4a3a`

**4. tshark 임상 이벤트 직접 캡처 구현**
- WCF 버퍼링 우회하여 `Laerdal.Event` 패킷 즉시 감지
- 이벤트 지연 문제 근본적 해결
- 커밋: `f350a0b`

**5. 수동 연결 모드 전환 및 로그 정리**
- 자동 연결 제거 → 수동 연결 모드로 변경
- UDP 브로드캐스트 중복 로그 제거
- WebSocket 브로드캐스트 로그 출력 조건 개선
- 커밋: `83c4d69`, `a000946`, `ed87928`

**6. SimMan WCF 임상 이벤트 감지 시도 및 롤백**
- SimMan에서 WCF로 임상 이벤트 직접 감지 시도
- 예상대로 동작하지 않아 롤백
- 커밋: `4d5b777`, `6c1072f`

**7. 번역 및 파라미터 지원 확장**
- LLEAP 표시와 일치하도록 한글 번역 추가
- 출혈 파라미터, 혈압 음 볼륨 파라미터 지원
- `PharyngealObstruction`, `Trismus` 전송 누락 수정
- `WcfEventCapture` 점(`.`) 제거 로직 삭제
- 커밋: `38deae3`, `a53d393`, `7daf104`, `3ee839c`, `09013c6`, `451d808`

**8. 문서화**
- `fix/zombie-and-lag-resolution` 작업 문서 추가
- 커밋: `556b2fc`

**9. Two-Track 아키텍처 설계**
- 성능 하향 평준화 Root Cause 분석
- `HANDOFF.md`에 구현 계획 문서화
- 다음 작업일 구현 예정

#### 기술적 세부사항

```
기존 아키텍처:
LLEAP → WCF 로그 → SimMonitor → SIMPREC
        (버퍼링 지연 발생)

하이브리드 트리거:
LLEAP → tshark (패킷 즉시 감지)  ─┬→ 먼저 감지된 쪽으로 세션 시작
      → WCF 로그 (기존 방식)    ─┘

전송 분리:
SESSION 이벤트 → HTTP (신뢰성 우선)
기타 이벤트   → WebSocket (속도 우선)
```

#### 배운 점

1. **하이브리드 접근법의 가치**: 단일 방식의 한계를 두 가지 방식 병렬로 극복
2. **전송 채널 분리**: 이벤트 성격에 따라 HTTP/WebSocket 적절히 분리
3. **롤백도 작업이다**: SimMan WCF 시도 → 롤백도 의미 있는 실험 기록
4. **문서화 습관**: 복잡한 버그 수정은 반드시 문서로 남겨야 나중에 이해 가능

#### 개인 회고

오늘은 정말 생산적인 하루였다. 18개 커밋이라니!

Zombie Data, Cold Start Lag, 중복 이벤트, 세션 미감지... 그동안 쌓여있던 골치 아픈 버그들을 하나씩 잡아나갔다. 특히 하이브리드 트리거 방식(tshark + WCF 병렬 감지)은 꽤 만족스러운 해결책이다. WCF 버퍼링 때문에 생기는 지연을 tshark 패킷 캡처로 우회하면서도, 기존 WCF 로직도 fallback으로 유지하는 방식.

SimMan WCF 임상 이벤트 직접 감지는 실패해서 롤백했지만, 이것도 의미 있는 실험이었다. "이 방법은 안 된다"는 것도 중요한 정보니까.

Two-Track 아키텍처 설계도 마무리했다. 성능 하향 평준화 문제의 Root Cause를 분석하고 해결 방향을 문서화해뒀으니, 다음 작업일에 바로 구현에 들어갈 수 있다.

30일의 답답함을 오늘 좀 풀었다. 역시 코드에 집중할 수 있는 날이 최고다.

---

## 기술적 성과

### 해결한 핵심 문제

| # | 문제 | 해결 방법 |
|---|------|----------|
| 1 | Docker에서 외부 WebSocket 접근 불가 | SSH 역방향 터널 + socat 우회 |
| 2 | DB 테이블 누락 | 마이그레이션 참조하여 수동 생성 + 모델 파일 추가 |
| 3 | LLEAP vs SimMonitor 로그명 불일치 | EventTranslations.cs 전면 수정 |
| 4 | 세션 파라미터 가독성 | LLEAP 디브리핑 형식으로 변환 로직 구현 |

### 수정된 주요 파일

**SimMonitor (C#)**
- `EventTranslations.cs` - 이벤트/약물 한글 번역 (~100개 항목)
- `ParameterTranslations.cs` - 파라미터 값 번역
- `SimulatorMonitor.cs` - 세션 파라미터 변환 로직
- `WcfEventCapture.cs` - 시나리오명 전달, 세션 시작/종료 이벤트
- `MainForm.cs` - SIMPREC 연동, 이벤트 핸들러

**SIMPREC (React/FastAPI)**
- `PlaybackEventLogPanel.tsx` - 기본/상세 보기 토글
- `MonitoringPage.tsx` - 실시간 모니터링 개선
- `recordings.py` - API 오류 수정
- `simmonitor_client.py` - 포트 변경 (Docker 우회)

---

## 운영 주의사항

### SimMonitor 실행 시

```bash
# 1. 태블릿 PC에서 SSH 터널 실행
ssh -R 18080:localhost:8080 ibstplan@10.10.61.137

# 2. 개발 PC에서 socat 실행
socat TCP-LISTEN:18081,fork,bind=0.0.0.0,reuseaddr TCP:127.0.0.1:18080 &

# 3. SimMonitor는 관리자 권한으로 실행 (카메라/패킷캡처 필요)
```

### 주의사항

- SimMonitor 관리자 권한 필수 (카메라 영상/tshark 패킷 캡처)
- WCF 로그 500MB 초과 시 자동 백업, 장시간 사용 시 정리 필요
- 세션 종료 응답 최대 30초 소요 가능

---

## 다음 단계

1. [ ] OP 서버 연동 및 미디어 스트리밍 아키텍처 구현 (HLS 패키징)
2. [ ] STT 음성 인식 기능 추가 (faster-whisper)
3. [ ] 프론트엔드 HLS.js 적용
4. [ ] GitLab CI/CD 파이프라인 구축

---

## 참고 문서

- `eth-simprec/eth.md` - 상세 작업 일지 및 기술 명세
- `simmonitor/CLAUDE.md` - SimMonitor 프로젝트 컨텍스트
- `eth-simprec/CLAUDE.md` - SIMPREC 프로젝트 개발 규칙

---

*최종 업데이트: 2026-01-23*
