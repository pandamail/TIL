# [D2] 1974. 스도쿠 검증
# 9×9 스도쿠 퍼즐이 올바른지 검사한다.
# 각 행·열·3×3 박스에 1~9가 중복 없이 모두 있으면 1, 아니면 0을 출력한다.
# 입력: T, 이후 각 테스트 케이스마다 9×9 행렬 (모든 칸은 1~9)
# 출력: #케이스번호 1 또는 #케이스번호 0

# ※ 미완성 — 행렬 입력까지만 작성. 검증 로직은 다음 날 이어서 작성 예정.

T = int(input())

for test_case in range(1, T + 1):

    matrix = []
    for _ in range(9):
        row = list(map(int, input().split()))
        matrix.append(row)
