# 🚀 Infrastructure

배포 환경 구성, 컨테이너 운영, CI/CD 파이프라인, 개발환경 세팅 기록입니다.

---

## 프로젝트별 문서

### SimMonitor (2026.01 ~ 2026.02)
의료 시뮬레이터 연동 미들웨어. C# + WPF + WebSocket 기반 실시간 데이터 파이프라인.

| 문서 | 요약 |
|------|------|
| [SimMonitor 아키텍처](2026-02-25-SimMonitor-Architecture.md) | 시스템 구조, 기술 스택, Mermaid 파이프라인 다이어그램 |
| [SimMonitor 트러블슈팅](2026-02-25-SimMonitor-Troubleshooting.md) | 개발 중 발생한 주요 이슈 및 해결 과정 |
| [SIMPREC 아키텍처](2026-02-25-Simprec-Architecture.md) | 녹화 관리 웹 시스템(FastAPI + React + MySQL) 구조 |
| [SIMPREC 트러블슈팅](2026-02-25-Simprec-Troubleshooting.md) | SIMPREC 연동 중 발생한 이슈 해결 |

### 러프씨 홈페이지 (2026.02 ~ 2026.03)
Next.js + Supabase + Vercel 기반 사진작가 포트폴리오 사이트.

| 문서 | 요약 |
|------|------|
| [개발환경 세팅 가이드](2026-02-rough-see-web-개발환경-세팅.md) | 새 PC에서 처음부터 셋업하는 전체 절차 (Node, Vercel CLI, Supabase CLI) |
| [Vercel + Supabase CLI 환경설정](2026-03-vercel-supabase-cli-환경설정.md) | CLI 연동 및 환경변수 관리 방법 |

---

## Docker

| 문서 | 요약 |
|------|------|
| [CBT 자동화 시스템 구축 회고](Docker/2025-09-17-CBT환경-자동배포-시스템-구축-회고.md) | Flask + Docker + Caddy로 학교별 CBT 환경을 자동 배포하는 PaaS 구축기 |
| [Docker 자동화 스크립트](Docker/2025-09-15-docker-automation.md) | Python으로 Docker 환경 자동 생성·관리 스크립트 작성 |
| [컨테이너 무한 루프 문제 해결](Docker/2025-09-16-컨테이너-무한-루프-문제-해결-과정.md) | 컨테이너 Restarting 상태 원인 분석 및 아키텍처 전환 과정 |
| [Docker 명령어 레퍼런스](Docker/commands.md) | 자주 쓰는 Docker 명령어 모음 |

---

## Jenkins

| 문서 | 요약 |
|------|------|
| [Jenkins Docker로 설치](Jenkins/2025-09-17-Jenkins-Docker로-설치.md) | Docker 컨테이너로 Jenkins 설치 및 초기 설정 |
| [Jenkins + GitLab Webhook 연동](Jenkins/2025-09-19-Jenkins-GitLab-Webhook-연동.md) | GitLab push 이벤트로 Jenkins 빌드 자동 트리거 설정 |
| [Jenkins Webhooks + Docker](Jenkins/2025-09-23-Jenkins-Webhooks-docker.md) | Webhook 기반 Docker 자동 배포 파이프라인 |
| [Pipeline Script 작성](Jenkins/2025-09-29-Pipeline-Script-작성.md) | Jenkinsfile 작성 방법 및 예제 |
| [GitLab 인증 토큰 만료 및 갱신](Jenkins/2025-10-17-GitLab-인증-토큰-만료-및-갱신-가이드.md) | 토큰 만료 시 Jenkins 연동 복구 절차 |
| [Jenkins 명령어 레퍼런스](Jenkins/commands.md) | 자주 쓰는 Jenkins 관련 명령어 모음 |

---

## Git

| 문서 | 요약 |
|------|------|
| [Git Rebase 실패 시 파일 복구](Git/2025-10-20-Git-Rebase-실패-시-VSCode-Local-History로-파일-복구하기.md) | rebase 중 미커밋 변경사항 유실 시 VSCode Local History로 복구하는 방법 |
