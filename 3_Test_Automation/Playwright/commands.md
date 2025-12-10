# Playwright 테스트 명령어

## 사전 준비

- **Node.js 버전**: `v18.12.0`

## 시작하기

1.  **프로젝트 클론**:
    ```bash
    git clone 
    ```

2.  **라이브러리 설치**:
    ```bash
    yarn install
    ```

## 테스트 실행 명령어

- **전체 테스트 (UI 모드)**:
  ```bash
  yarn test
  ```

- **디버그 모드**:
  ```bash
  yarn test -- --debug
  ```

- **특정 파일 테스트**:
  ```bash
  yarn test -- --target [folder_name/file_name]
  ```

- **여러 대상 테스트 (순차 실행)**:
  ```bash
  yarn test -- --target [folder_1] [folder_2] --serial
  ```

- **이름으로 특정 테스트 케이스 실행**:
  ```bash
  yarn test -- --g "test name"
  ```

- **특정 브라우저로 테스트**:
  ```bash
  yarn test -- --project [chromium|firefox|webkit]
  ```
