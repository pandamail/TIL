# [Playwright] CI 환경 테스트 안정화 전략: 순차 실행과 방어적 탐색

## 문제 상황

Jenkins와 같은 CI 환경에서 Playwright 테스트를 병렬로 실행할 때, 다수의 테스트가 동시에 DB 상태를 변경(예: 시험 생성, 삭제)하면서 **경쟁 상태(Race Condition)**가 발생했다. 이로 인해 특정 테스트가 검증해야 할 항목(예: 방금 생성한 시험)이 목록의 2페이지 이후로 밀려나, 1페이지만 확인하는 테스트가 항목을 찾지 못하고 타임아웃으로 실패하는 문제가 빈번했다.

## 최종 해결 전략: 하이브리드 방식

이 문제를 해결하기 위해, 두 가지 전략을 조합한 하이브리드 방식을 채택하여 테스트의 안정성을 극대화했다.

### 1. `test.describe.serial`을 이용한 순차 실행

가장 먼저, 관련된 테스트 그룹을 `test.describe.serial` 블록으로 묶어 순차 실행을 강제했다.

```typescript
// exam.spec.ts
test.describe.serial('시험 관리 순차 테스트', () => {
  test('1. 새로운 시험을 생성할 수 있다.', async ({ page }) => {
    // ... 시험 생성 로직
  });

  test('2. 생성된 시험을 복사할 수 있다.', async ({ page }) => {
    // ... 시험 복사 로직
  });

  test('3. 원본 시험을 삭제할 수 있다.', async ({ page }) => {
    // ... 시험 삭제 로직
  });
});
```

- **효과**: 이 구조는 테스트들이 정의된 순서대로 실행되는 것을 보장한다. '시험 생성' 테스트가 완료되기 전에 '시험 삭제' 테스트가 실행되는 등의 경쟁 상태를 원천적으로 차단하여 데이터 일관성을 확보한다.

### 2. `ensureExamIsVisible` 헬퍼를 이용한 방어적 탐색

순차 실행만으로는 해결되지 않는 문제가 있다. 바로 **UI 렌더링 지연**이나, 예기치 않은 이유로 발생하는 **페이지네이션**이다. 이를 해결하기 위해, 특정 항목이 화면에 나타날 때까지 모든 페이지를 탐색하는 방어적 코드를 추가했다.

```typescript
// common/util.ts
export async function ensureExamIsVisible(page: Page, examName: string) {
  const examRowLocator = page.locator(`tr:has-text("${examName}")`);

  while (!(await examRowLocator.isVisible())) {
    const nextButton = page.getByText('›', { exact: true });
    if (await nextButton.isVisible()) {
      await nextButton.click();
      await page.waitForLoadState('networkidle'); // 페이지 전환 대기
    } else {
      throw new Error(`시험 '${examName}'을 모든 페이지에서 찾을 수 없습니다.`);
    }
  }
}
```

- **효과**: 테스트가 특정 시험을 찾아야 할 때, 이 함수를 호출하면 현재 페이지에 없더라도 '다음' 버튼을 눌러가며 끝까지 추적한다. 이는 순차 실행 중 발생할 수 있는 렌더링 지연이나 다른 잠재적 변수로부터 테스트를 보호하는 **이중 안전장치** 역할을 한다.

## 결론

- **`test.describe.serial`**: 테스트 간의 **데이터 충돌(경쟁 상태)**을 막는다.
- **`ensureExamIsVisible`**: 테스트 내에서 **UI의 동적인 변화(페이지네이션, 렌더링 지연)**에 대응한다.

이 두 가지 전략을 함께 사용함으로써, CI 환경에서 간헐적으로 실패하던 E2E 테스트의 안정성과 신뢰도를 크게 향상시킬 수 있었다.
