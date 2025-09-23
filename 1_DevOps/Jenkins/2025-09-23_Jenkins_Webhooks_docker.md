어제까지 진행 상황

cbt-test-pipeline의 경로에 cbt admin, user, 백엔드, db 4개의 프로젝트를 webhooks 걸고 push event를 했을 때 Jenkins에서 4개의 프로젝트 이벤트 모두 Hello worlds!가 출력이 잘 되는 상태까지 진행 완료.


오늘 해야 할 일
오늘은 어제 진행했던 cbt-test-pipeline에서 이제 push event를 발생시켰을 때 cbt admin, user, 백엔드, db가 모두 clone 되고, docker로 build를 완료하고 caddy를 사용하여 playwright, Jmetter를 테스트 할 수 있는 환경이 되기까지의 환경을 만드는 게 오늘의 목표.

우선 처음에 hello world! 만 뜨던 환경에서 cbt-test-pipeline에 기존 프로젝트를 클론하는 작업을 먼저 진행했음.

step by step으로 해야 하기 위해서

clone -> docker build -> caddy 순으로 하고자 했음.

# 그래서 1단계인 clone을 어떻게 했느냐

1. 제미나이가 처음에 node.js 버전을 맞추기 위해 nvm을 써야하니, registry.ibst-cbt.kr/에서 이미지를 불러와야 하니.. 이런 이상한 실수를 하길래 바로잡았음. 그렇게 하는게 아니라 gitlab에 프로젝트 clone해놓은 걸 가져와서 사용할 것이라고.

2. 아래 코드를 pipeline script에 집어넣고 clone에 성공함. 맨 처음에 
'your-gitlab-credentials' 에 EomTaeHyeok을 집어넣었으나 Jenkins에 추가했던 내 계정이 eth여서 이를 수정하니 빌드 성공. push event를 발생시키니 clone하는 로직이 잘 작동함.

pipeline {
    agent any // 아무 Jenkins 에이전트에서나 실행

    stages {
        // --- 1단계: 모든 소스 코드 클론 ---
        stage('Checkout All Sources') {
            steps {
                // Jenkins 작업 공간을 깨끗하게 정리하고 시작합니다.
                cleanWs()

                echo "1. 필요한 모든 소스 코드를 각각의 폴더에 클론합니다."
                
                // dir: 이 블록 안의 명령어는 지정된 하위 폴더에서 실행됩니다.
                dir('admin') {
                    echo "=> admin 레포지토리 클론 중..."
                    // 님의 개인 GitLab 'admin' 레포지토리 주소와 브랜치를 입력하세요.
                    // credentialsId는 Jenkins에 등록된 GitLab 계정 정보 ID입니다.
                    git branch: 'main', url: 'http://10.10.61.143:9999/EomTaeHyeok/admin.git', credentialsId: 'your-gitlab-credentials'
                }
                dir('user') {
                    echo "=> user 레포지토리 클론 중..."
                    git branch: 'main', url: 'http://10.10.61.143:9999/EomTaeHyeok/user.git', credentialsId: 'your-gitlab-credentials'
                }
                dir('backend') {
                    echo "=> be-cbt 레포지토리 클론 중..."
                    git branch: 'main', url: 'http://10.10.61.143:9999/EomTaeHyeok/be-cbt.git', credentialsId: 'your-gitlab-credentials'
                }
                dir('db') {
                    echo "=> db 레포지토리 클론 중..."
                    git branch: 'main', url: 'http://10.10.61.143:9999/EomTaeHyeok/db.git', credentialsId: 'your-gitlab-credentials'
                }

                echo "모든 소스 코드 클론 완료!"
            }
        }


# 2단계 빌드

cbt-test-pipeline에서 이제 clone된 파일을 build하는 과정을 해보려고 했는데
스크립트를 수정하고 push event를 발생시키니 docker: not found 오류가 나왔음.

docker: not found -> 도커를 찾을 수 없다. jenkins 내에 도커가 설치되지 않았다는 아주 간단한 오류인데 처음에는 docker image가 깔려있는 곳을 제대로 못찾나? 라는 생각을 했었음..

- 리눅스 가상머신에 jenkins 사용자를 docker 그룹에 추가함
- agent {
        docker {
            image 'docker:latest' 
            args '-v /var/run/docker.sock:/var/run/docker.sock' 
        }
    }
    docker pipeline 플러그인도 설치한 후에 이렇게 바꿔서 해봤음. 그래도 안되서 선임분의 코드를 봤더니 agent any로 되어있었음.
    docker pipeline 플러그인은 사용하지 않으시는 것 같고..

- 그냥 "docker" 이 자체가 없었던거임. jenkins에. 

해결방법
- docker exec -it -u root jenkins-lts bash로 가상환경의 jenkins 컨테이너 접속
- apt-get update
- apt-get install -y docker.io
- which docker
- docker --version

도커 설치 완료

- docker restart jenkins-lts
jenkins 재부팅

도커를 설치한 후 다시 push event를 해보니 build가 성공적으로 진행되었음.

Finished: SUCCESS
<!-- 
# 3단계 

pipeline {
    agent any

    // 요청하신 환경 변수를 여기에 정의합니다.
    environment {
        GITLAB_HOST = "10.10.61.143" // GitLab 서버 주소
        HOST_IP = "10.10.61.191"     // 사용자 VM의 IP 주소
        PROJECT_NAME = "cbt-project" // 프로젝트 식별 이름
        RUN_COUNT = 5                // (필요 시 사용하는 변수)
        
        // Jenkins 빌드 번호를 모든 이미지의 공통 버전(태그)으로 사용합니다.
        IMAGE_TAG = "build-${env.BUILD_NUMBER}"
    }

    stages {
        // --- 1단계: 소스 코드 클론 ---
        stage('Checkout All Sources') {
            steps {
                cleanWs()
                echo "1. 현재 작업 에이전트(${env.NODE_NAME})에 소스 코드를 클론합니다."
                
                dir('admin') {
                    git branch: 'main', url: "http://${GITLAB_HOST}:9999/EomTaeHyeok/admin.git", credentialsId: 'eth'
                }
                dir('user') {
                    git branch: 'main', url: "http://${GITLAB_HOST}:9999/EomTaeHyeok/user.git", credentialsId: 'eth'
                }
                dir('backend') {
                    git branch: 'main', url: "http://${GITLAB_HOST}:9999/EomTaeHyeok/be-cbt.git", credentialsId: 'eth'
                }
                dir('db') {
                    git branch: 'main', url: "http://${GITLAB_HOST}:9999/EomTaeHyeok/db.git", credentialsId: 'eth'
                }
            }
        }
        
        // --- 2단계: 로컬 Docker 이미지 빌드 ---
        stage('Build Docker Images Locally') {
            parallel {
                stage('Build Admin') {
                    steps {
                        dir('admin') {
                            sh "docker build -t cbt-admin:${IMAGE_TAG} ."
                        }
                    }
                }
                stage('Build User') {
                    steps {
                        dir('user') {
                            sh "docker build -t cbt-user:${IMAGE_TAG} ."
                        }
                    }
                }
                stage('Build Backend') {
                    steps {
                        dir('backend') {
                            sh "docker build -t cbt-backend:${IMAGE_TAG} ."
                        }
                    }
                }
                stage('Build DB') {
                    steps {
                        dir('db') {
                            sh "docker build -t cbt-db:${IMAGE_TAG} ."
                        }
                    }
                }
            }
        }
    }
} -->

