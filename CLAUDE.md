# Claude Code 프로젝트 컨텍스트

## 프로젝트 개요
- **목적**: 개인 TIL(Today I Learned) 및 기술 학습 기록 저장소
- **주요 기술**: Docker, Jenkins, Flask, DevOps
- **시작일**: 2025-09-15

## 최근 작업 내역
### 2025-09-18
- ✅ README.md 링크 오류 수정
- ✅ .gitignore 파일 생성
- ✅ 카테고리별 commands.md 파일 분리 (Docker, Jenkins)
- ✅ 각 카테고리 README.md 생성 (DevOps, Backend)
- ✅ TIL 템플릿 2종 생성 (문제해결형, 일상기록형)

## 프로젝트 구조
```
TIL/
├── 1_DevOps/
│   ├── Docker/        # Docker 관련 학습 및 명령어
│   ├── Jenkins/       # Jenkins CI/CD 관련
│   └── README.md      # DevOps 학습 로드맵
├── 2_Backend/
│   └── README.md      # Backend 개발 노트
├── 3_Wep_concepts/    # (오타 수정 필요: Web_concepts)
├── TIL/              # 일일 학습 기록
│   ├── template_problem_solving.md  # 문제해결 템플릿
│   └── template_daily_log.md       # 일상기록 템플릿
├── COMMANDS.md       # 마스터 명령어 모음
└── README.md         # 프로젝트 메인 문서

## 주요 작업 내용
- **CBT 자동화 시스템**: Docker Compose를 활용한 멀티테넌트 환경 구축
- **Jenkins 설정**: 포트 9090에서 실행 (Caddy와 충돌 방지)
- **Flask 백엔드**: CBT 시스템 API 서버 구현

## 코딩 규칙
- 날짜 형식: YYYY-MM-DD (예: 2025-09-18)
- 파일명: 날짜_제목.md (공백 대신 언더스코어 사용)
- 커밋 메시지: 한글 사용 가능, 간결하게 작성

## 다음 작업 예정
- [ ] 폴더명 오타 수정: 3_Wep_concepts → 3_Web_concepts
- [ ] 파일명 규칙 통일 (공백 제거)
- [ ] GitHub Actions 자동화 설정
- [ ] 태그 시스템 도입

## 자주 사용하는 명령어
```bash
# Git 상태 확인 및 커밋
git status
git add .
git commit -m "메시지"
git push

# Docker 컨테이너 확인
docker ps
docker logs [container_name]

# 프로젝트 빌드 및 테스트
npm run lint
npm run typecheck
```

## 사용자 정보
- **수준**: 주니어 개발자 (실무 경험 있음)
- **선호 설명 방식**: 실용적, 예제 중심
- **호칭**: 편하게 반말 사용 선호
- **관심 분야**: DevOps, 자동화, 실무 적용 가능한 기술

## 주의사항
- 민감한 정보(비밀번호, API 키) 절대 커밋하지 않기
- .gitignore 파일 항상 확인
- 코드 수정 후 lint 및 typecheck 실행