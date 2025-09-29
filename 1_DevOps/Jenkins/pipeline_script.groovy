def tag_names = [:] // 컨테이너/이미지 태그명
tag_names['FE_ADMIN'] = 'dev_cbt_fe_admin'
tag_names['FE_USER']  = 'dev_cbt_fe_user'
tag_names['BE']       = 'dev_cbt_be'
tag_names['DB']       = 'dev_cbt_db'
tag_names['TEST']     = 'dev_cbt_test'
tag_names['JMETER']   = 'dev_cbt_jmeter'

pipeline {
  agent any

  environment {
    GITLAB_HOST  = "10.10.61.143"
    HOST_IP      = "10.10.61.191"
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
              url: "http://10.10.61.143:9999/EomTaeHyeok/admin.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout FE_USER') {
      steps {
        dir("FE_USER") {
          git branch: "main",
              url: "http://10.10.61.143:9999/EomTaeHyeok/user.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout BE') {
      steps {
        dir("BE") {
          git branch: "dev",
              url: "http://10.10.61.143:9999/EomTaeHyeok/be-cbt.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout DB') {
      steps {
        dir("DB") {
          git branch: "dev",
              url: "http://10.10.61.143:9999/cbt/db.git",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout Test') {
      steps {
        dir("Test") {
          git branch: "main",
              url: "http://10.10.61.143:9999/EomTaeHyeok/test",
              credentialsId: 'eth'
        }
      }
    }
    stage('Checkout JMeter') {
      steps {
        dir("JMeter") {
          git branch: "sample", // ← 대소문자 맞춤
              url: "http://10.10.61.143:9999/EomTaeHyeok/jmeter",
              credentialsId: 'eth'
        }
      }
    }

    /* ========== BUILD (병렬) ========== */
    stage('Build Projects') {
      parallel {
        stage('Build FE_ADMIN') { steps { dir("${env.WORKSPACE}/FE_ADMIN") { sh "docker build --no-cache -t ${tag_names['FE_ADMIN']} ." } } }
        stage('Build FE_USER')  { steps { dir("${env.WORKSPACE}/FE_USER")  { sh "docker build --no-cache -t ${tag_names['FE_USER']}  ." } } }
        stage('Build BE')       { steps { dir("${env.WORKSPACE}/BE")       { sh "docker build --no-cache -t ${tag_names['BE']}       ." } } }
        stage('Build DB')       { steps { dir("${env.WORKSPACE}/DB")       { sh "docker build --no-cache -t ${tag_names['DB']}       ." } } }
        stage('Build PlayWright'){steps { dir("${env.WORKSPACE}/Test")     { sh "docker build --no-cache -t ${tag_names['TEST']}     ." } } }
        stage('Build JMeter')   { steps { dir("${env.WORKSPACE}/JMeter")   { sh "docker build --no-cache -t ${tag_names['JMETER']}   ." } } }
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
          sh """
            echo "[WAIT] DB ready ..."
            for i in {1..60}; do
              docker exec ${tag_names['DB']} sh -lc "mysqladmin ping -uroot -p'Ibst0552997730!'" && break
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
              -e DB_PASSWORD=Ibst0552997730! \
              -e DB_NAME=CBT \
              -e MAIL_USER=ibstechco \
              -e MAIL_PASSWORD=ibst0552997730! \
              -e MAIL_USER_EMAIL=ibstechco@naver.com \
              -e MAIL_TEXT=${env.HOST_IP}:20008/Result/ \
              -e DATA_PATH=/data \
              -e EXCEL_PATH=/excelData \
              -e IMP_KEY=0588880683871737 \
              -e IMP_SECRET=4cf0204264167f2102a9b791a3d69b6d2cb680f7cb348e89d393421335d5527c366d5b01155454da \
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

            docker cp ${tag_names['FE_ADMIN']}:/app/build ./fe_admin_build_output
            docker cp ${tag_names['FE_USER']}:/app/build ./fe_user_build_output

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

        /* === 요청사항: ADMIN env.js 생성 (하드코딩) === */
        script {
          sh """
            docker exec -i caddy sh -lc 'mkdir -p /etc/caddy/CBT/dev_cbt/FE_ADMIN'
            docker exec -i caddy sh -lc 'cat > /etc/caddy/CBT/dev_cbt/FE_ADMIN/env.js << "EOF"
window.env = { BE_HOST: "http://${env.HOST_IP}:30006", USERNAME: "DEV", VERSION: "dev" };
EOF
'
            docker exec caddy sh -lc "nl -ba /etc/caddy/CBT/dev_cbt/FE_ADMIN/env.js | sed -n '1,40p'"
          """
        }

        /* === 요청사항: USER index.html 51번 라인에 else-if 블록 삽입 === */
        script {
          sh '''
            # 1) 패치 스크립트 로컬 생성 (보간/특수문자 안전)
            cat > /tmp/patch_user.sh <<'EOS'
set -eu
f=/etc/caddy/CBT/dev_cbt/FE_USER/index.html
if [ ! -f "$f" ]; then
  echo "[WARN] $f not found, skipping patch"
  exit 0
fi
needle='else if (ADDRESS.includes(":30005")) { LOCATION = "DEV_USER"; API_PORT = "30006"; // BE 포트 }'
if ! grep -Fq "$needle" "$f"; then
  lines=$(wc -l < "$f" 2>/dev/null || echo 0)
  if [ "$lines" -ge 51 ]; then
    awk -v ins="$needle" 'NR==51{print ins} {print}' "$f" > "$f.tmp" && mv "$f.tmp" "$f"
  else
    echo "$needle" >> "$f"
  fi
else
  echo "[INFO] snippet already present"
fi
EOS
            # 2) 컨테이너로 복사해서 실행 & 프리뷰
            docker cp /tmp/patch_user.sh caddy:/tmp/patch_user.sh
            docker exec caddy sh -lc 'sh /tmp/patch_user.sh && sed -n "45,60p" /etc/caddy/CBT/dev_cbt/FE_USER/index.html || true'
          '''
        }

        /* PlayWright 컨테이너 (대기 실행) */
        script {
          sh """
            echo "[INFO] PlayWright 테스트 컨테이너 실행 시작"
            docker rm -f ${tag_names['TEST']} || true
            docker run -d --name ${tag_names['TEST']} \
              --network dev_cbt_network \
              -e ADMIN_HOST=http://${env.HOST_IP}:${env.PORT_ADMIN} \
              -e ADMIN_ID=admin \
              -e ADMIN_PASSWORD=1234 \
              -e USER_HOST=http://${env.HOST_IP}:${env.PORT_USER} \
              -e USER_ID=user \
              -e USER_PASSWORD=1234 \
              -e DB_HOST=${tag_names['DB']} \
              -e DB_PORT=3306 \
              -e DB_USER=root \
              -e DB_PASSWORD=Ibst0552997730! \
              -e DB_NAME=CBT \
              ${tag_names['TEST']} bash -c "tail -f /dev/null"
          """
        }

        /* JMeter 컨테이너 */
        script {
          sh """
            echo "[INFO] JMETER 테스트 컨테이너 실행 시작"
            docker rm -f ${tag_names['JMETER']} || true
            docker run -d --name ${tag_names['JMETER']} \
              --network dev_cbt_network \
              -e TARGET_HOST=${tag_names['BE']} \
              -e TARGET_PORT=4000 \
              -e TARGET_DB_HOST=${tag_names['DB']} \
              -e TARGET_DB_PORT=3306 \
              -e TARGET_DB_USER=root \
              -e TARGET_DB_PW=Ibst0552997730! \
              -e TARGET_DB_NAME=CBT \
              -e RUN_COUNT=${env.RUN_COUNT} \
              ${tag_names['JMETER']} bash -c "mkdir -p /app/jmeter-report && tail -f /dev/null"

            docker exec ${tag_names['JMETER']} bash /app/generate_data.sh || true
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

    /* ========== JMeter ========== */
    stage('JMeter') {
      steps {
        script {
          def jmx_files = sh(
            script: """
              docker exec ${tag_names['JMETER']} sh -c '
                for dir in \$(find /app/tests -mindepth 1 -maxdepth 1 -type d | sort); do
                  find "\$dir" -type f -name "*.jmx" | sort
                done
              '
            """,
            returnStdout: true
          ).trim().split('\\n')

          for (f in jmx_files) {
            def relpath     = f.replaceFirst('/app/tests/', '')
            def base        = relpath.replaceAll(/\\.jmx$/, '')
            def report_file = "${base.replaceAll('/', '_')}.jtl"

            sh """ echo "[INFO] ${report_file} FILE Test Start" """

            def loop = env.RUN_COUNT.toInteger()
            def loopx2 = loop * 2
            def loopxloop = loop * loop
            def loopxloopx2 = loop * loop * 2

            sh """
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${domain}|${tag_names['BE']}|g" ${f}
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${port}|4000|g" ${f}
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${loop}|${loop}|g" ${f}
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${loopx2}|${loopx2}|g" ${f}
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${loopxloop}|${loopxloop}|g" ${f}
              docker exec ${tag_names['JMETER']} sed -i "s|\\\${loopxloopx2}|${loopxloopx2}|g" ${f}
              docker exec ${tag_names['JMETER']} jmeter -n -t '${f}' -l /app/jmeter-report/${report_file}
            """
          }

          sh '''
            docker exec ''' + tag_names['JMETER'] + ''' sh -lc '
              first_file=true
              for jtl in $(find /app/jmeter-report -name "*.jtl" | sort); do
                echo "[DEBUG] merging $jtl"
                if [ "$first_file" = true ]; then
                  head -n 1 "$jtl" > /app/jmeter-report/total.jtl
                  tail -n +2 "$jtl" >> /app/jmeter-report/total.jtl
                  first_file=false
                else
                  tail -n +2 "$jtl" >> /app/jmeter-report/total.jtl
                fi
              done
              wc -l /app/jmeter-report/*.jtl || true
            '
          '''

          echo "[INFO] 리포트 생성"
          sh "docker exec ${tag_names['JMETER']} jmeter -g /app/jmeter-report/total.jtl -o /app/jmeter-report/final_report"

          sh """
            rm -rf ./jmeter-report
            docker cp ${tag_names['JMETER']}:/app/jmeter-report ./jmeter-report || true
          """

          publishHTML([
            reportDir: 'jmeter-report/final_report',
            reportFiles: 'index.html',
            reportName: 'Jmeter Report',
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
          ])

          // 아카이브(보기 편하게)
          sh "(zip -r jmeter-report.zip jmeter-report || tar -czf jmeter-report.tgz jmeter-report) || true"
          archiveArtifacts artifacts: 'jmeter-report.zip,jmeter-report.tgz', allowEmptyArchive: true
        }
      }
    }
  } // stages

  post {
    always {
      script {
        echo "[Project Delete] 테스트 컨테이너/이미지 삭제"
        sh """
          docker rm -f ${tag_names['TEST']} ${tag_names['JMETER']} || true
          docker rmi -f ${tag_names['TEST']} ${tag_names['JMETER']} || true
        """
      }
    }
  }
}
