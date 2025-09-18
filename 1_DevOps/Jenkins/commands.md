# Jenkins 명령어 모음

## 🚀 Jenkins 설치 및 실행

### Docker로 Jenkins 실행
```bash
# 최초 설치 (포트 9090 사용)
docker run -d \
  -p 9090:8080 \
  -v jenkins_home:/var/jenkins_home \
  --name jenkins-lts \
  jenkins/jenkins:lts-jdk17

# Jenkins 시작/중지
docker start jenkins-lts
docker stop jenkins-lts
docker restart jenkins-lts
```

### 초기 설정
```bash
# 초기 관리자 비밀번호 확인
docker exec jenkins-lts cat /var/jenkins_home/secrets/initialAdminPassword

# Jenkins 로그 확인
docker logs jenkins-lts
```

## 🔧 Jenkins 관리

### 플러그인 관리
```bash
# Jenkins CLI 다운로드
wget http://localhost:9090/jnlpJars/jenkins-cli.jar

# 플러그인 목록 확인
java -jar jenkins-cli.jar -s http://localhost:9090/ list-plugins
```

### 백업 및 복구
```bash
# Jenkins 홈 디렉토리 백업
docker exec jenkins-lts tar czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp jenkins-lts:/tmp/jenkins-backup.tar.gz ./

# 복구
docker cp jenkins-backup.tar.gz jenkins-lts:/tmp/
docker exec jenkins-lts tar xzf /tmp/jenkins-backup.tar.gz -C /
```

## 💡 유용한 팁
- Caddy와 함께 사용 시 포트 충돌 주의 (8000-9000 범위 피하기)
- Jenkins 홈 디렉토리는 반드시 볼륨 마운트로 영구 보존
- 정기적인 백업 스케줄 설정 권장