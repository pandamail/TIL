# [Jenkins] Docker를 이용한 Jenkins 설치 및 Freestyle Job 생성

## 🤔 문제 상황
- 리눅스(Ubuntu) 환경에 CI/CD 자동화를 위한 Jenkins 설치가 필요했다.
- 초기에는 `apt` 패키지 매니저로 설치를 시도했으나, Java 경로 문제와 포트 충돌(`address already in use`) 등 복잡한 설정 오류가 계속 발생했다.
- `.war` 파일을 직접 실행하는 방식도 시도했으나, 터미널 세션이 종료되면 함께 꺼지는 문제가 있었다.

## 💡 해결 과정
- 가장 안정적이고 격리된 환경을 제공하는 **Docker를 이용해 Jenkins를 설치**하는 방식을 최종적으로 선택했다.
- 아래 명령어를 사용하여, Jenkins 데이터를 호스트 PC에 영구적으로 저장(`-v jenkins_home:/var/jenkins_home`)하고, `9090` 포트로 실행되는 Jenkins 컨테이너를 성공적으로 생성했다.

- (9090 포트로 실행했던 이유는...)
    -> 최초 1회는 위 명령어로 jenkins 설치를 진행함. 현재 caddy에서 8000-9000 포트를 관리하고 있기 때문에 Jenkins의 포트를 9090에서 열었음.

```bash
  docker run -d -p 9090:8080 -v jenkins_home:/var/jenkins_home --name jenkins-lts jenkins/jenkins:lts-jdk17

  docker start jenkins-lts
```

🚀 첫 번째 Job 실행: "Hello World"
Jenkins의 가장 기본적인 기능인 Freestyle project를 생성했다.

**'Build Steps'**에서 **'Execute shell'**을 추가하고, echo "Hello World" 라는 간단한 셸 스크립트를 작성했다.

**'Build Now'**를 통해 작업을 실행하고, **'Console Output'**에서 "Hello World"가 성공적으로 출력되는 것을 확인하며 Jenkins의 기본 작동 원리를 이해했다.

🌟 오늘의 성과
GitLab 플러그인을 설치하고, cbt-admin, cbt-user, cbt-v2, cbt-db 등 모든 CBT 프로젝트의 소스 코드를 Git을 통해 성공적으로 클론하는 Job 설정을 완료했다.

이를 통해 CI(Continuous Integration) 파이프라인의 첫 단계인 '코드 통합' 과정을 자동화할 준비를 마쳤다.