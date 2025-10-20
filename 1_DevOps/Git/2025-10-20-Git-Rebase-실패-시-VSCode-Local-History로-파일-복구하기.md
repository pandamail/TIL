# Git Rebase 실패 시 VS Code의 Local History로 파일 복구하기

`git rebase`는 커밋 히스토리를 깔끔하게 정리하는 강력한 도구지만, 익숙하지 않을 경우 의도치 않게 작업 내용을 날려버릴 수 있는 위험한 명령어이기도 하다. 특히 **커밋하지 않은 변경사항(uncommitted changes)**이 있을 때 `rebase`를 실행하면 해당 내용이 유실될 수 있다.

이 문서에서는 `git rebase` 과정에서 커밋하지 않은 로컬 파일 변경사항이 사라졌을 때, VS Code의 `Local History` 기능을 사용해 복구하는 방법을 기록한다.

## 문제 상황

`git rebase -i <commit-hash>` 명령어를 사용해 여러 커밋을 하나로 합치거나(squash) 순서를 변경하는 과정에서 충돌(conflict)이 발생하거나, 사용자의 실수로 인해 아직 커밋하지 않고 작업 중이던 파일의 내용이 사라지는 경우가 발생할 수 있다.

이때 많은 개발자들이 `git reflog`를 떠올리지만, 이는 해결책이 될 수 없다.

> **`git reflog`가 동작하지 않는 이유**
> `git reflog`는 HEAD가 변경되었던 **커밋(commit) 기록**을 추적한다. 따라서 커밋되지 않은, 즉 로컬 작업 디렉토리(Working Directory)에만 존재하던 변경사항은 `reflog`에 기록되지 않으므로 복구할 수 없다.

## 해결책: VS Code의 Local History 활용

Git으로 복구할 수 없는 상황에서 VS Code의 `Local History` 기능은 매우 유용한 구원투수가 될 수 있다. 이 기능은 Git과 별개로, VS Code가 파일의 변경 이력을 자동으로 저장해두는 기능이다.

### 복구 절차

1.  **명령어 팔레트 열기**
    - `Ctrl+Shift+P` (macOS: `Cmd+Shift+P`)를 눌러 명령어 팔레트를 연다.

2.  **Local History 기능 실행**
    - 팔레트에 `Local History`를 입력하면 관련된 여러 명령어가 나타난다.
    - 이 중에서 **`Local History: Find Entry to Restore`** 를 선택한다.

    ![VSCode Local History](https://user-images.githubusercontent.com/15308189/136703132-1e566a62-a13a-48a2-888b-935d98374b6f.png)

3.  **복구할 내용 확인 및 복원**
    - 실행하면 VS Code가 추적하고 있던 모든 파일의 변경 이력 목록이 시간순으로 나타난다.
    - 검색창에 유실된 파일명을 입력하여 범위를 좁힐 수 있다.
    - 복구하고 싶은 시점의 기록을 선택하면 해당 시점의 파일 내용이 열리며, 현재 파일과 비교(diff)하여 보여준다.
    - 내용을 확인하고, 상단의 `Restore` 버튼이나 편집기 창의 옵션을 통해 파일 전체 또는 특정 부분만 복원할 수 있다.

## 예방이 최선: `git stash` 생활화

이러한 파일 유실 사태를 미연에 방지하는 가장 좋은 방법은, 위험한 Git 명령어를 실행하기 전에 현재 작업 내용을 안전하게 임시 저장하는 것이다.

```bash
# rebase, reset 등 위험한 작업을 하기 전 습관적으로 실행
git stash
```

`git stash`를 실행하면 커밋하지 않은 모든 변경사항이 스택(stack)에 임시 저장된다. `rebase` 작업이 끝난 후, `git stash pop` 명령어로 저장해두었던 내용을 다시 안전하게 불러올 수 있다.

---

**태그**: #Git #VScode #Troubleshooting #Rebase #Stash #LocalHistory
