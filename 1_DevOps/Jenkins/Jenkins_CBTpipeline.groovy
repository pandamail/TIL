// 보안상 민감한 정보는 블러처리 (aaaa)
// 태그명 정의
def tag_names = [:] // 컨테이너/이미지 태그명
tag_names['FE_ADMIN'] = 'dev_cbt_fe_admin'
tag_names['FE_USER']  = 'dev_cbt_fe_user'
tag_names['BE']       = 'dev_cbt_be'
tag_names['DB']       = 'dev_cbt_db'
tag_names['TEST']     = 'dev_cbt_test'
// tag_names['JMETER']   = 'dev_cbt_jmeter'

pipeline {
  agent any

  // IP는 보안상 블러처리 (aaaa)
  environment {
    GITLAB_HOST  = "aaaa"
    HOST_IP      = "aaaa"
    PROJECT_NAME = "projects"
    RUN_COUNT    = 5
  }

  stages {
    stage('info') {
      steps {
        script {
          echo "▶ Project Name: ${env.gitlabSourceRepoName}"
          echo "▶ Branch: ${env.gitlabSourceBranch}"
          echo "▶ User: ${env.gitlabUserName}"
          echo "▶ Event: ${env.gitlabActionType}"
        }
      }
    }

    /* ========== CHECKOUT ========== */
    stage('Checkout FE_ADMIN') {
      steps {
        dir("FE_ADMIN") {
          git branch: "dev",
              url: "http://aaaa:9999/EomTaeHyeok/admin.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout FE_USER') {
      steps {
        dir("FE_USER") {
          git branch: "main",
              url: "http://aaaa:9999/EomTaeHyeok/user.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout BE') {
      steps {
        dir("BE") {
          git branch: "dev",
              url: "http://aaaa:9999/EomTaeHyeok/be-cbt.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout DB') {
      steps {
        dir("DB") {
          git branch: "dev",
              url: "http://aaaa:9999/cbt/db.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout Test') {
      steps {
        dir("Test") {
          git branch: "main",
              url: "http://aaaa:9999/EomTaeHyeok/test",
              credentialsId: 'eth'
        }
      }
    }
    /*
    stage('Checkout JMeter') {
      steps {
        dir("JMeter") {
          git branch: "sample", // ← 대소문자 맞춤
              url: "http://aaaa:9999/EomTaeHyeok/jmeter",
              credentialsId: 'eth'
        }
      }
    }
    */

    /* ========== BUILD (병렬) ========== */
    stage('Build Projects') {
      parallel {
        stage('Build FE_ADMIN') { steps { dir("${env.WORKSPACE}/FE_ADMIN") { sh "docker build --no-cache -t ${tag_names['FE_ADMIN']} ." } } }
        stage('Build FE_USER')  { steps { dir("${env.WORKSPACE}/FE_USER")  { sh "docker build --no-cache -t ${tag_names['FE_USER']}  ." } } }
        stage('Build BE')       { steps { dir("${env.WORKSPACE}/BE")       { sh "docker build --no-cache -t ${tag_names['BE']}       ." } } }
        stage('Build DB')       { steps { dir("${env.WORKSPACE}/DB")       { sh "docker build --no-cache -t ${tag_names['DB']}       ." } } }
        stage('Build PlayWright'){steps { dir("${env.WORKSPACE}/Test")     { sh "docker build --no-cache -t ${tag_names['TEST']}     ." } } }
        // stage('Build JMeter')   { steps { dir("${env.WORKSPACE}/JMeter")   { sh "docker build --no-cache -t ${tag_names['JMETER']}   ." } } }
      }
    }

    /* ========== RUNTIME 준비/실행 ========== */
    stage('Project Run') {
      steps {
        /* 네트워크 보장 */
        script {
          sh """
            if ! docker network ls --format '{{ .Name }}' | grep -wq dev_cbt_network; then
              docker network create dev_cbt_network
            fi
            if ! docker network ls --format '{{ .Name }}' | grep -wq caddy_network; then
              docker network create caddy_network
            fi
          """
        }

        /* Caddy 기동/설정 준비 */
        script {
          sh """
            if docker ps -a --format '{{ .Names }}' | grep -wq caddy; then
              echo "[INFO] caddy already running"
            else
              echo "[INFO] Run caddy"
              docker run -d --name caddy \
                --network caddy_network \
                -p 80:80 -p 443:443 \
                -p 30004:30004 -p 30005:30005 \
                -v caddy_data:/data -v caddy_config:/config \
                --restart always caddy:2.7.6-alpine
            fi
            docker exec caddy mkdir -p /etc/caddy/sites
            if ! docker exec caddy grep -q "import /etc/caddy/sites/*" /etc/caddy/Caddyfile; then
              docker exec caddy sh -c "echo '\\nimport /etc/caddy/sites/*' >> /etc/caddy/Caddyfile"
            fi
          """
        }

        /* FE 컨테이너 (빌드 산출물 추출용) */
        script {
          sh """
            echo "[INFO] FE_ADMIN run (for build copy)"
            docker rm -f ${tag_names['FE_ADMIN']} || true
            docker run -d --name ${tag_names['FE_ADMIN']} \
              -e BE_HOST= -e USERNAME=DEV -e VERSION=dev \
              --network dev_cbt_network ${tag_names['FE_ADMIN']}

            echo "[INFO] FE_USER run (for build copy)"
            docker rm -f ${tag_names['FE_USER']} || true
            docker run -d --name ${tag_names['FE_USER']} \
              -e BE_HOST= -e USERNAME=DEV -e VERSION=dev \
              --network dev_cbt_network ${tag_names['FE_USER']}
          """
        }

        /* DB → BE 순서 (DB 준비 대기 포함) */
        script {
          sh """
            echo "[INFO] DB run"
            docker rm -f ${tag_names['DB']} || true
            docker run -d --name ${tag_names['DB']} \
              --restart always \
              -p ${env.HOST_IP}:30007:3306 \
              -e MARIADB_ROOT_PASSWORD=Ibst0552997730! \
              -e MARIADB_DATABASE=CBT \
              -e LANG=C.UTF-8 \
              -v dev_cbt_db_data:/var/lib/mysql \
              --network dev_cbt_network ${tag_names['DB']} \
              bash -c "/wait_for_afterdb.sh & docker-entrypoint.sh mysqld"
          """

          // DB 준비 대기 (최대 60초)
          // DB 정보는 보안상 블러처리 (aaaa)
          sh """
            echo "[WAIT] DB ready ..."
            for i in {1..60}; do
              docker exec ${tag_names['DB']} sh -lc "mysqladmin ping -uroot -p'aaaa'" && break
              sleep 1
            done
          """

          sh """
            echo "[INFO] BE run"
            docker rm -f ${tag_names['BE']} || true
            docker run -d --name ${tag_names['BE']} \
              --restart always \
              -p ${env.HOST_IP}:30006:4000 \
              -e SWAGGER_SCHEMES=http \
              -e SWAGGER_HTTP_KIND=http:// \
              -e SWAGGER_HOST=0.0.0.0 \
              -e SWAGGER_PORT=4000 \
              -e DB_HOST=${tag_names['DB']} \
              -e DB_PORT=3306 \
              -e DB_USER=root \
              -e DB_PASSWORD=aaaa \
              -e DB_NAME=CBT \
              -e MAIL_USER=aaaa \
              -e MAIL_PASSWORD=aaaa \
              -e MAIL_USER_EMAIL=aaaa@naver.com \
              -e MAIL_TEXT=${env.HOST_IP}:20008/Result/ \
              -e DATA_PATH=/data \
              -e EXCEL_PATH=/excelData \
              -e IMP_KEY=aaaa \
              -e IMP_SECRET=aaaa \
              --network dev_cbt_network ${tag_names['BE']}
            docker network connect caddy_network ${tag_names['BE']}
          """
        }

        /* FE 빌드 산출물 → Caddy 복사 */
        script {
          sh """
            echo "[INFO] copy FE build to caddy"
            docker exec caddy mkdir -p /etc/caddy/CBT/dev_cbt/FE_ADMIN
            docker exec caddy mkdir -p /etc/caddy/CBT/dev_cbt/FE_USER

	    echo "[INFO] 기존 빌드 아웃풋 디렉토리 정리"
            rm -rf ./fe_admin_build_output
	    rm -rf ./fe_user_build_output

	    echo "[INFO] FE 컨테이너에서 빌드 결과 추출"
            docker cp ${tag_names['FE_ADMIN']}:/app/build ./fe_admin_build_output
            docker cp ${tag_names['FE_USER']}:/app/build ./fe_user_build_output

	    echo "[INFO] 빌드 결과를 Caddy 컨테이너로 복사"
            docker cp ./fe_admin_build_output/. caddy:/etc/caddy/CBT/dev_cbt/FE_ADMIN
            docker cp ./fe_user_build_output/. caddy:/etc/caddy/CBT/dev_cbt/FE_USER
          """
        }

        /* Caddy 라우팅 설정 & 리로드 */
        script {
          def port_admin = 30004
          def port_user  = 30005
          env.PORT_ADMIN = port_admin
          env.PORT_USER  = port_user

          def caddy_config = """
            :${env.PORT_ADMIN} {
              root * /etc/caddy/CBT/dev_cbt/FE_ADMIN
              route {
                reverse_proxy /api/* ${tag_names['BE']}:4000
                reverse_proxy /data/* ${tag_names['BE']}:4000
                try_files {path} /index.html
                file_server
              }
            }
            :${env.PORT_USER} {
              root * /etc/caddy/CBT/dev_cbt/FE_USER
              route {
                reverse_proxy /api/* ${tag_names['BE']}:4000
                reverse_proxy /data/* ${tag_names['BE']}:4000
                try_files {path} /index.html
                file_server
              }
            }
          """.stripIndent().trim()

          writeFile file: "dev_cbt.caddy", text: caddy_config
          sh """
            docker cp dev_cbt.caddy caddy:/etc/caddy/sites/dev_cbt.caddy
            docker exec caddy caddy reload --config /etc/caddy/Caddyfile
          """
        }


        /* PlayWright 컨테이너 (대기 실행) */
        // DB 정보는 보안상 블러처리 (aaaa)
        script {
          sh """
            echo "[INFO] PlayWright 테스트 컨테이너 실행 시작"
            docker rm -f ${tag_names['TEST']} || true
            docker run -d --name ${tag_names['TEST']} \
              --network dev_cbt_network \
              -e ADMIN_HOST=http://${env.HOST_IP}:${env.PORT_ADMIN} \
              -e ADMIN_ID=aaaa \
              -e ADMIN_PASSWORD=aaaa \
              -e USER_HOST=http://${env.HOST_IP}:${env.PORT_USER} \
              -e USER_ID=user \
              -e USER_PASSWORD=aaaa \
              -e DB_HOST=${tag_names['DB']} \
              -e DB_PORT=aaaa \
              -e DB_USER=aaaa \
              -e DB_PASSWORD=aaaa \
              -e DB_NAME=CBT \
              ${tag_names['TEST']} bash -c "tail -f /dev/null"
          """
        }
      }
    }

    /* ========== PlayWright (현재 비활성화) ========== */
    stage('PlayWright') {
      steps {
        script {
          sh 'rm -rf ./playwright-report'
          sh "docker exec ${tag_names['TEST']} rm -rf /app/playwright-report || true"

          def test_failed = false
          echo "[PlayWright] 테스트 시작"
          try {
            sh """
              docker exec -e PW_TEST_HTML_REPORT_OPEN=never ${tag_names['TEST']} \
              npx playwright test --reporter=html
            """
          } catch (err) {
            echo "[PlayWright] 일부 실패 - 리포트는 계속 출력"
            test_failed = true
          }

          sh "docker cp ${tag_names['TEST']}:/app/playwright-report ./playwright-report || true"
          if (test_failed) { currentBuild.result = 'FAILURE' }

          archiveArtifacts artifacts: 'playwright-report/**', allowEmptyArchive: true

          // zip 없을 때도 빌드 안 깨지게
          sh "(zip -r playwright-report.zip playwright-report || tar -czf playwright-report.tgz playwright-report) || true"
          archiveArtifacts artifacts: 'playwright-report.zip,playwright-report.tgz', allowEmptyArchive: true
        }
        script {
          publishHTML([
            reportDir: 'playwright-report',
            reportFiles: 'index.html',
            reportName: 'Playwright Report',
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
          ])
        }
      }
    }

      } // stages

  post {
    always {
      script {
        echo "[Project Delete] 테스트 컨테이너/이미지 삭제"
        sh """
          docker rm -f ${tag_names['TEST']} || true
          docker rmi -f ${tag_names['TEST']} || true
        """
      }
    }
  }
}
