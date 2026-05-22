# [D2] 1974. 스도쿠 검증
# 9×9 스도쿠 퍼즐에서 각 행·열·3×3 박스에 1~9가 중복 없이 있으면 1, 아니면 0을 출력한다.
# 입력: T, 이후 각 테스트 케이스마다 9×9 행렬 (모든 칸은 1~9)
# 출력: #케이스번호 1 또는 #케이스번호 0

def check_sudoku(matrix):
    # 가로 행 검사
    for i in range(9):
        if len(set(matrix[i])) != 9:
            return 0

    # 세로 열 검사
    for j in range(9):
        col = [matrix[i][j] for i in range(9)]
        if len(set(col)) != 9:
            return 0

    # 3×3 박스 검사
    for i in range(0, 9, 3):
        for j in range(0, 9, 3):
            box = []
            for x in range(3):
                for y in range(3):
                    box.append(matrix[i + x][j + y])
            if len(set(box)) != 9:
                return 0

    return 1

T = int(input())

for test_case in range(1, T + 1):
    matrix = [list(map(int, input().split())) for _ in range(9)]
    ans = check_sudoku(matrix)
    print(f'#{test_case} {ans}')


# ※ 꼭 기억할 것
# 1. 중복 검사: len(set(배열)) != 9 — set으로 중복 제거 후 길이가 9이면 1~9 모두 있음
# 2. 세로 열 추출: [matrix[i][j] for i in range(9)] — 리스트 컴프리헨션으로 한 줄 추출
# 3. 3×3 박스 시작점: range(0, 9, 3) → 0, 3, 6 — 스텝 3으로 꼭짓점을 잡는 테크닉
# 4. 내부 루프 변수: 바깥 루프(i, j)와 안쪽 루프(x, y) 이름이 겹치면 덮어써서 버그 발생
