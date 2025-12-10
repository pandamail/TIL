# Docker 명령어 모음

## 🚀 CBT 자동화 시스템

### 시스템 실행
```bash
# 마스터 Caddy 실행 (최초 1회)
docker compose -f docker-compose.caddy.yml up -d

# 특정 환경 실행 (예: kn)
docker compose -f ./generated_services/CBT/kn/docker-compose.yml -p kn up -d
```

### 환경 관리
```bash
# 완전 삭제 (컨테이너 + 데이터)
docker compose -f ./generated_services/CBT/kn/docker-compose.yml -p kn down -v
sudo rm -rf ./generated_services/CBT/kn

# 로그 확인
docker logs cbt_kn_be
docker logs -f cbt_kn_be  # 실시간 로그
```

### 문제 해결
```bash
# Caddy 포트 충돌 해결
sudo rm -f ./common/caddy/sites/*.caddy

# 빌드 & 배포
yarn build
cp -r ~/projects/admin/build/* ./common/www/admin
```

## 🧹 시스템 관리

### 정리 명령어
```bash
# 사용하지 않는 리소스 정리
docker system prune -a

# 특정 이미지만 삭제
docker rmi $(docker images -q <image_name>)

# 중지된 컨테이너 정리
docker container prune
```

### 모니터링
```bash
# 실행 중인 컨테이너
docker ps

# 디스크 사용량 확인
docker system df

# 컨테이너 리소스 사용량
docker stats
```

## 💡 유용한 팁
- 컨테이너 이름은 프로젝트명_서비스명_역할 규칙 사용 (예: cbt_kn_db)
- 로그는 주기적으로 확인하여 문제 조기 발견
- 빌드 캐시는 정기적으로 정리하여 디스크 공간 확보