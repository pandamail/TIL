# Git Rebase 실패 시 VS Code의 Local History로 파일 복구하기

## 🔍 문제 상황
`git rebase` 명령 사용 중 커밋하지 않은 로컬 파일 변경사항이 유실되는 경우가 발생할 수 있다.
`git reflog`는 커밋된 기록만 추적하므로, 커밋되지 않은 변경사항은 `reflog`로 복구할 수 없다.
> 이를 인지하지 못하고 실행하여 커밋하지 않은 파일이 모두 유실된 상태.

## 💡 해결책: VS Code의 Local History 활용
VS Code의 `Local History` 기능은 Git과 별개로 파일의 변경 이력을 자동으로 저장해두므로, 
Git으로 복구 불가능한 상황에서 유용하게 사용할 수 있다.

### 복구 절차
1.  **명령어 팔레트 열기**: `Ctrl+Shift+P`
2.  **`Local History: Find Entry to Restore` 실행**: 명령어 팔레트에 `Local History` 입력 후 해당 옵션 선택.
3.  **복구할 내용 확인 및 복원**: 시간순으로 나타나는 변경 이력 목록에서 유실된 파일명을 검색하여 찾고, 
    복구할 시점을 선택하여 내용을 확인한 뒤 `Restore` 버튼으로 복원한다.

## ✅ 예방이 최선: `git stash` 생활화
`git rebase`, `git reset` 등 잠재적으로 위험한 Git 명령어를 실행하기 전에는 반드시 `git stash`를 사용하여 현재 작업 내용을 임시 저장하는 습관을 들이는 것이 좋습니다. 작업 완료 후 `git stash pop`으로 저장해두었던 내용을 다시 불러올 수 있다.

```bash
# rebase, reset 등 위험한 작업을 하기 전 습관적으로 실행
git stash
```

---

**태그**: #Git #VScode #Troubleshooting #Rebase #Stash #LocalHistory
