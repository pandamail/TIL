## **Docker CBT 자동화 시스템 관리 명령어 모음**

### **1. 시스템 전체 실행 (개발 시작)**

> 매일 개발을 시작할 때 사용하는 명령어 순서

1.  **마스터 Caddy 실행 (최초 1회 또는 재부팅 후):**

      * **위치:** `~/projects/cbt-manager`
      * **명령어:** `docker compose -f docker-compose.caddy.yml up -d`

2.  **Python 가상 환경 활성화:**

      * **위치:** `~/projects/cbt-manager`
      * **명령어:** `source venv/bin/activate`

3.  **백엔드 API 서버 실행 (터미널 1):**

      * **위치:** `~/projects/cbt-manager`
      * **명령어:** `python api/main.py`

4.  **프론트엔드 UI 서버 실행 (터미널 2):**

      * **위치:** `~/projects/cbt-manager/frontend`
      * **명령어:** `python3 -m http.server 7070`

-----

### **2. 특정 CBT 환경 관리**

> UI로 생성한 특정 학교(예: `kn`)의 환경을 제어할 때

  * **완전히 삭제하기 (컨테이너 + 데이터 + 폴더):**

    1.  **Docker 리소스 삭제:**
          * **위치:** `~/projects/cbt-manager`
          * **명령어:** `docker compose -f ./generated_services/CBT/kn/docker-compose.yml -p kn down -v`
    2.  **폴더 삭제:**
          * **위치:** `~/projects/cbt-manager`
          * **명령어:** `sudo rm -rf ./generated_services/CBT/kn`

  * **로그 확인하기 (백엔드 예시):**

      * **위치:** 아무 곳이나
      * **명령어:** `docker logs cbt_kn_be`

-----

### **3. Docker 시스템 전체 관리**

  * **모든 Docker 리소스 대청소 (실행 중인 컨테이너 제외):**

      * **설명:** 사용하지 않는 모든 이미지, 중지된 컨테이너, 네트워크, 빌드 캐시를 삭제하여 디스크 공간을 확보합니다.
      * **명령어:** `docker system prune -a`

  * **실행 중인 컨테이너 목록 확인:**

      * **명령어:** `docker ps`

-----

### **4. 데이터베이스 직접 수정**

> 특정 학교(`kn`) DB에 접속하여 데이터를 직접 수정해야 할 때

1.  **DB 컨테이너 접속:**

      * **명령어:** `docker exec -it cbt_kn_db mysql -u root -p`
      * (비밀번호 `Ibst0552997730!` 입력)

2.  **데이터베이스 선택 및 수정:**

      * **명령어 (SQL):**
        ```sql
        USE CBT;
        ALTER TABLE sub_student_exam_tb MODIFY student_sit INT NOT NULL DEFAULT 0;
        exit;
        ```