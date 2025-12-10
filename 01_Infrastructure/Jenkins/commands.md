# Jenkins 명령어 모음

## 🔗 GitLab 연동

### GitLab Webhook 설정
```bash
# GitLab 프로젝트 Clone
git clone http://[gitlab-server]/[username]/[project-name].git


### VS Code Remote-SSH 설정 수정 (IP 변경 시)
```bash
# ~/.ssh/config 파일 수정
Host [hostname]
    HostName [new-ip-address]
    User [username]
    Port 22
```

## 💡 유용한 팁
- Caddy와 함께 사용 시 포트 충돌 주의 (8000-9000 범위 피하기)
- Jenkins 홈 디렉토리는 반드시 볼륨 마운트로 영구 보존
- 정기적인 백업 스케줄 설정 권장
- **네트워크 대역 확인**: GitLab과 Jenkins는 같은 네트워크 대역에 있어야 Webhook 연동 가능
- **Jenkins 속도 저하 시**: Jenkins URL 설정이 올바른지 확인
- **IP 변경 시 체크리스트**:
  - SSH 설정 업데이트
  - Jenkins System 설정 업데이트