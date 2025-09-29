# Jenkins-GitLab Webhook 연동 및 네트워크 문제 해결

## 📅 작성일: 2025-09-19

## 🎯 목표
GitLab 프로젝트를 Jenkins와 연동하여 Webhook을 통한 자동 빌드 환경 구성

## 📝 작업 내용

### 1. GitLab 프로젝트 생성 및 Clone
- GitLab에서 새로운 프로젝트 생성 (Import project 기능 사용)
- 로컬 환경에 프로젝트 Clone
```bash
git clone http://10.10.61.143/[username]/[project-name].git
```

### 2. 네트워크 문제 발생 및 원인 분석

#### 문제 상황
- Jenkins에서 GitLab Webhook 연결 실패
- 동료는 정상 작동, 본인만 연결 불가

#### 원인
- **네트워크 대역 불일치**
  - 회사 GitLab 서버: `10.10.61.143`
  - 내 리눅스 가상환경: `192.168.0.100`
  - 내 Windows: `192.168.0.xxx`
- WiFi 공유기를 통한 별도 네트워크 연결로 회사 인터넷망과 분리됨

### 3. 문제 해결 과정

#### 3.1 네트워크 연결 변경
1. 랜선을 회사 인터넷망으로 직접 연결
2. 리눅스 가상환경 IP 주소 변경
   - 기존: `192.168.0.100`
   - 변경: `10.10.61.xxx` (회사 네트워크 대역)

#### 3.2 VS Code Remote-SSH 설정 수정
```ssh
# ~/.ssh/config 수정
Host linux-vm
    HostName 10.10.61.xxx  # 새로운 IP 주소로 변경
    User [username]
    Port 22
```

#### 3.3 Jenkins 재시작 및 설정
```bash
docker start jenkins-lts
```

#### 3.4 Jenkins 시스템 설정 수정
- Jenkins 관리 → System → Jenkins URL 수정
- 새로운 IP 주소에 맞게 URL 업데이트
- 예: `http://10.10.61.xxx:9090`

#### 3.5 GitLab 플러그인 재설치
1. Jenkins 관리 → 플러그인 관리
2. GitLab 플러그인 재설치
3. Jenkins 재부팅

### 4. Webhook 연동 성공
- GitLab과 Jenkins 간 Webhook 연결 성공
- Hello World 프로젝트 빌드 테스트 완료

## 🔧 주요 설정값

### Jenkins 설정
- Jenkins URL: `http://10.10.61.xxx:9090`
- GitLab Connection 설정 완료

### GitLab Webhook 설정
- Webhook URL: `http://10.10.61.xxx:9090/project/[project-name]`
- Trigger: Push events

## 💡 배운 점
1. **네트워크 대역 일치의 중요성**: 서로 다른 네트워크 대역에 있는 서비스는 직접 통신이 불가능
2. **IP 변경 시 연관 설정 체크리스트**:
   - SSH 설정 (VS Code Remote-SSH config)
   - Docker 컨테이너 내부 설정
   - Jenkins System 설정
3. **체계적인 문제 해결 접근법**: 네트워크 문제 → IP 확인 → 설정 수정 → 재시작

## 🔍 트러블슈팅 팁
- Jenkins가 느려질 때: Jenkins URL 설정 확인
- Webhook 연결 실패 시: 네트워크 대역 먼저 확인
- IP 변경 후: 모든 연관 서비스의 설정 업데이트 필요

## 📌 참고사항
- Jenkins 포트: 9090 (Caddy와 충돌 방지)
- GitLab 서버: 10.10.61.143
- 회사 네트워크 대역: 10.10.61.0/24