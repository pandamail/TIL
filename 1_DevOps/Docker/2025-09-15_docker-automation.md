# 2025-09-15: Docker 환경 자동 배포 시스템 구축

## 🤔 오늘 해결한 문제 (Problem)
- **목표:** 웹 UI에서 버튼 클릭 시, 학교별로 격리된 CBT(FE, BE, DB) Docker 환경을 동적으로 생성하는 자동화 시스템의 백엔드를 구축해야 했다.
- **초기 문제:** Docker와 컨테이너, 이미지의 개념이 모호했고, 여러 개의 컨테이너를 어떻게 하나의 시스템으로 묶어서 관리해야 할지 막막했다.

---

## 💡 어떻게 해결했나 (Solution)

### 1. **아키텍처 설계: 중앙 관제 리버스 프록시**
- 처음에는 각 환경마다 FE/BE/DB 컨테이너를 모두 띄우려 했으나, 선임분의 실제 운영 환경 분석을 통해 **'마스터 Caddy'**가 모든 요청을 받아 처리하는 '리버스 프록시' 구조가 더 효율적임을 파악하고 아키텍처를 변경했다.
- 이 구조 덕분에 포트 충돌 문제 없이 서비스를 무한정 확장할 수 있는 기반을 마련했다.

### 2. **자동화 스크립트 구현 (Python, Flask)**
- 사용자의 요청을 받을 API 서버를 **Flask**로 구축했다.
- API 요청이 오면, Python의 `os`, `shutil`, `subprocess` 모듈을 사용해 다음과 같은 작업을 자동화했다.
    - BE/DB 환경을 정의하는 `docker-compose.yml` 파일 동적 생성
    - DB 초기화를 위한 `.sql` 스크립트 자동 복사
    - `docker compose up -d` 명령어 실행
    - 마스터 Caddy의 라우팅 규칙 파일(`.caddy`) 생성 및 `caddy reload` 실행

### 3. **로컬 테스트 환경 구축**
- 실제 도메인 없이 로컬 환경에서 테스트하기 위해 두 가지 방식을 탐색했다.
    - **(초기 방식) 가상 도메인:** 윈도우 **`hosts` 파일**에 `192.168.0.100 admin-kn.local`과 같이 가상 도메인을 수동 매핑하여 접속했다.
    - **(최종 방식) IP/포트:** 마스터 Caddy가 포트 번호로 사이트를 구분하도록 설정을 변경하여, `hosts` 파일 수정 없이 `http://192.168.0.100:8001`과 같이 직접 접속하는 더 간편한 구조를 채택했다.
- Caddy가 자동으로 생성하는 로컬 HTTPS의 `ERR_CERT_AUTHORITY_INVALID` 오류는, Caddy 컨테이너에서 Root CA 인증서(`root.crt`)를 추출해 윈도우 PC의 '신뢰할 수 있는 루트 인증 기관'에 설치하여 해결했다.



---


## 🔗 참고 자료 (Reference)
- [Caddy 공식 문서 - reverse_proxy](https://caddyserver.com/docs/caddyfile/directives/reverse_proxy)
- [Docker 공식 문서 - Compose file](https://docs.docker.com/compose/compose-file/)