# 📘 Today I Learned - 엄태혁

꾸준한 학습과 실습을 통해 개발 역량을 쌓고,
문제의 근본 원인을 분석해 스스로 해결한 경험을 기록합니다.
AI 도구를 적극 활용한 실무 중심 개발을 지향합니다.

---

## 🙋 About

경영학에서 컴퓨터공학으로 전과한 개발자. 문과 출신으로 시작해 스스로 방향을 바꿨고, 인턴십에서 실무 개발 경험을 쌓았습니다.

| 항목 | 내용 |
|------|------|
| 학력 | 경남대학교 컴퓨터공학부 졸업 (2026.02) |
| 전과 | 경영학과 → 컴퓨터공학부 (2023.03) |
| 병역 | 대한민국 육군 병장 만기전역 (2021.03 ~ 2022.09) |

**IBST 기업부설연구소** · 연구원(인턴) · 2025.08 ~ 2026.02 (6개월)
경남대학교 4학년 현장실습. CBT 환경 자동화, E2E 테스트 자동화, SimMonitor 미들웨어 개발.

---

## 📂 Categories

| 폴더 | 내용 |
|---|---|
| [`00_Troubleshooting`](00_Troubleshooting/) | 에러 해결 경험, 원인 분석 기록 |
| [`01_Infrastructure`](01_Infrastructure/) | Docker, Jenkins, Vercel, CLI 환경설정, 배포 |
| [`02_Backend`](02_Backend/) | 서버 동작 원리, Supabase DB, API, 환경 분리 |
| [`03_Test_Automation`](03_Test_Automation/) | Playwright 기반 자동화 테스트 |
| [`98_Algorithm`](98_Algorithm/) | 알고리즘 풀이 기록 |
| [`99_TIL`](99_TIL/) | 일별 작업 로그 (YYYY/MM/YYYY-MM-DD 형식) |

---

## 🗂️ Projects

### 러프씨 홈페이지 (2026.02 ~ 2026.03)
Claude Code를 활용해 약 2주 만에 개발한 Next.js 기반 사진작가 포트폴리오 사이트.
Vercel 배포, Supabase DB 연동, dev/prod 환경 분리까지 단독 진행.
→ [rough-see.vercel.app](https://rough-see.vercel.app) · [개발환경 세팅](01_Infrastructure/2026-02-rough-see-web-개발환경-세팅.md) · [트러블슈팅](00_Troubleshooting/2026-02-rough-see-web-트러블슈팅.md)

### SimMonitor (2025.08 ~ 2026.02, 인턴)
의료 시뮬레이터 연동 미들웨어 단독 개발. LLEAP 시뮬레이터와 녹화 관리 시스템 사이의 실시간 데이터 파이프라인 구축.
→ [SimMonitor 아키텍처](01_Infrastructure/2026-02-25-SimMonitor-Architecture.md) · [SimMonitor 트러블슈팅](01_Infrastructure/2026-02-25-SimMonitor-Troubleshooting.md)
→ [SIMPREC 아키텍처](01_Infrastructure/2026-02-25-Simprec-Architecture.md) · [SIMPREC 트러블슈팅](01_Infrastructure/2026-02-25-Simprec-Troubleshooting.md)
→ [프로젝트 전체 회고록](01_Infrastructure/RETROSPECTIVE_2026-01.md)

### CBT 자동화 시스템 (2025.09, 인턴)
Flask + Docker + Caddy로 구축한 학교별 CBT 환경 자동 배포 시스템.
→ [구축 회고](01_Infrastructure/Docker/2025-09-17-CBT환경-자동배포-시스템-구축-회고.md)

### Playwright E2E 테스트 자동화 (2025.10 ~ 2026.01, 인턴)
CBT 시스템의 로그인부터 시험 응시까지 step1~6 전 플로우를 커버하는 E2E 테스트 스위트 단독 구축.
Jenkins CI 환경에서 발생한 Race Condition(병렬 실행 시 DB 경합)을 `serial` 실행 + 방어적 탐색 하이브리드 전략으로 해결했으며,
테스트 실패를 분석하는 과정에서 실제 서비스 코드의 비동기 버그를 발견·수정하는 성과로 이어짐. Chromium·Firefox·WebKit 크로스 브라우저 전 구간 통과.
→ [개발 작업일지 (22세션, 600줄+)](03_Test_Automation/Playwright/2025-10-CBT-Playwright-개발-작업일지.md) · [CI 안정화 전략](03_Test_Automation/Playwright/2025-10-21-테스트안정화_전략.md)

---

## 📝 Recent TIL

- [SSAFY D-4 — 확실한 문제 3개 확보](99_TIL/2026/05/2026-05-19-SSAFY적성진단-알고리즘D4.md) — 정렬·단어퍼즐 통과, 2차원 배열 탐색 패턴 정리
- [SSAFY D-5 — 에세이 제출 + 알고리즘 D2](99_TIL/2026/05/2026-05-18-SSAFY적성진단-에세이제출-알고리즘D2.md) — 지원 에세이 제출, 슬라이딩·회전 패턴 학습
- [SSAFY 알고리즘 3일차 — 달팽이 문제](99_TIL/2026/05/2026-05-14-SSAFY적성진단-알고리즘D2달팽이.md) — 나선형 탐색, 방향 배열과 % 4 순환 전환
- [SSAFY 알고리즘 2일차 — D2 격자 탐색 도전](99_TIL/2026/05/2026-05-13-SSAFY적성진단-알고리즘D2도전.md) — 파리 퇴치 4중 루프, 격자 탐색 공식 체득
- [머신러닝을 활용한 데이터 분석 (중급)](99_TIL/2026/05/2026-05-12-머신러닝-데이터분석-중급.md) — 앙상블 6종·변수선택·BERT Fine-tuning, 정형→비정형 성능 계단식 향상
- [SSAFY 적성진단 대비 알고리즘 D1 시작](99_TIL/2026/05/2026-05-12-SSAFY적성진단-알고리즘D1시작.md) — D1 문제 12개+D2 1개, 오랜만에 알고리즘 복귀
- [데이터 분석 기초 및 데이터 활용 이해 강의](99_TIL/2026/05/2026-05-11-데이터분석기초-데이터활용이해-강의.md) — 민간 데이터 특성, 분석 기획 4단계, 교차·지수화·시계열 분석

---

## 🎓 Education & Courses

| 과정 | 기관 | 시기 |
|------|------|------|
| [머신러닝을 활용한 데이터 분석](99_TIL/2026/05/2026-05-12-머신러닝-데이터분석-중급.md) | 경남 빅데이터 허브 | 2026.05 |
| [데이터 분석 기초 및 데이터 활용 이해](99_TIL/2026/05/2026-05-11-데이터분석기초-데이터활용이해-강의.md) | 경남 빅데이터 허브 | 2026.05 |
| [Google Gemini를 활용한 Agent 기반 자동화](99_TIL/2026/05/2026-05-06-Gemini-Agent-자동화-강의-1일차.md) | 경남 빅데이터 허브 | 2026.05 |
| [빅데이터이해 온라인강좌](99_TIL/2026/05/2026-05-06-빅데이터이해-온라인강좌.md) | 경남 빅데이터센터 | 2026.05 |

---

## 📬 Contact

Email: eomtaehyeok01@gmail.com
GitHub: https://github.com/pandamail/TIL
