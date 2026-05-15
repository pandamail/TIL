# [D2] 1954. 달팽이 숫자
# N×N 행렬을 달팽이 모양(나선형)으로 1부터 N²까지 채워서 출력한다.
# 핵심: 방향 배열(우→하→좌→상) + 벽/방문 체크 시 방향 전환
#   - dx, dy: 이동 방향 (우, 하, 좌, 상)
#   - 다음 칸이 범위 초과 또는 이미 채워진 경우 → 방향을 오른쪽으로 90도 전환
# 입력: T, 이후 각 테스트 케이스마다 N
# 출력: #케이스번호, 이후 N줄에 걸쳐 N×N 행렬

T = int(input())

for test_case in range(1, T + 1):

    N = int(input())
    matrix = [[0] * N for _ in range(N)]

    x, y = 0, 0

    # 방향: 우→하→좌→상
    dx = [0, 1, 0, -1]  # 세로(행)
    dy = [1, 0, -1, 0]  # 가로(열)

    direction = 0

    for num in range(1, N * N + 1):
        matrix[x][y] = num

        nx = x + dx[direction]
        ny = y + dy[direction]

        if nx < 0 or nx >= N or ny < 0 or ny >= N or matrix[nx][ny] != 0:
            direction = (direction + 1) % 4
            nx = x + dx[direction]
            ny = y + dy[direction]

        x, y = nx, ny

    print(f"#{test_case}")
    for row in matrix:
        print(*row)
