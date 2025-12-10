# Gemini 로컬 Playwright 테스트 가이드

이 문서는 로컬 환경에서 Playwright 테스트를 설정하고 실행하는 방법에 대한 핵심 정보를 요약합니다.

## 1. 환경 설정

1.  **`.env.test` 파일 생성:**
    *   프로젝트의 루트 디렉터리에서 `.env.test.sample` 파일을 복사하여 `.env.test`라는 이름의 새 파일을 만듭니다.

2.  **환경 변수 입력:**
    *   생성된 `.env.test` 파일을 열고, 실제 테스트 환경에 맞는 정보(서버 주소, ID, 비밀번호 등)를 각 변수에 맞게 입력합니다.

## 2. 의존성 설치

*   프로젝트에 필요한 라이브러리들을 설치하기 위해 터미널에서 아래 명령어를 실행합니다.

    ```bash
    # yarn을 사용하는 경우
    yarn
    ```

## 3. 테스트 실행

*   터미널에서 아래 명령어를 사용하여 테스트를 실행할 수 있습니다.

*   **전체 테스트 실행 (순차적):**
    ```bash
    yarn test
    ```

*   **UI 모드로 실행 (디버깅에 유용):**
    *   테스트 실행 과정을 시각적으로 확인하며 디버깅할 수 있습니다.
    ```bash
    npx playwright test --ui
    ```

*   **특정 파일 또는 폴더만 테스트:**
    *   `--target` 인자를 사용합니다. `yarn test` 뒤에 `--`를 붙여야 인수가 제대로 전달됩니다.
    ```bash
    yarn test -- --target tests/step1/login.spec.ts

    corepack yarn test -- --target tests/step1/login.spec.ts
    corepack yarn test -- --target tests/step2/question.spec.ts
    corepack yarn test -- --target tests/step3/setting.spec.ts
    corepack yarn test -- --target tests/step4/exam.spec.ts
    ```

*   **이름가 일치하는 테스트만 실행:**
    *   `-g` (grep) 인자를 사용합니다.
    ```bash
    yarn test -- --g "로그인 성공"
    ```
