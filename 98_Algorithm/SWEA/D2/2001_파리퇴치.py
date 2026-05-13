# [D2] 2001. 파리 퇴치
# N×N 격자에서 M×M 크기의 파리채로 잡을 수 있는 파리의 최댓값을 구한다.
# 핵심: 4중 루프 — (i,j)는 파리채 시작점, (x,y)는 파리채 내부 탐색점
#   - 시작점 범위: range(N - M + 1) → 파리채가 격자를 벗어나지 않도록
#   - 탐색 범위: range(i, i + M), range(j, j + M)
# 입력: T, 이후 각 테스트 케이스마다 N M, 그 다음 N×N 행렬
# 출력: #케이스번호 최댓값

T = int(input())

for test_case in range(1, T + 1):
    N, M = map(int, input().split())
    matrix = []
    for _ in range(N):
        row = list(map(int, input().split()))
        matrix.append(row)

    ans = 0
    for i in range(N - M + 1):
        for j in range(N - M + 1):
            current_sum = 0
            for x in range(i, i + M):
                for y in range(j, j + M):
                    current_sum += matrix[x][y]
            if current_sum > ans:
                ans = current_sum

    print(f"#{test_case} {ans}")
