# [D4] 1226. 미로1
# 16×16 미로에서 출발점(2)에서 도착점(3)까지 경로가 존재하는지 DFS로 판별한다.
# 벽=1, 통로=0, 출발=2, 도착=3
# 입력: 테스트 케이스 번호 + 16줄의 미로 (T 없이 10회 고정)
# 출력: #케이스번호 1(경로 있음) 또는 0(없음)

dx = [0, 1, 0, -1]
dy = [1, 0, -1, 0]

def dfs(x, y):
    for i in range(4):
        nx = x + dx[i]
        ny = y + dy[i]

        if 0 <= nx < 16 and 0 <= ny < 16:
            if matrix[nx][ny] == 3:
                return 1
            elif matrix[nx][ny] == 0:
                matrix[nx][ny] = 1  # 방문 처리 (발자국)
                if dfs(nx, ny) == 1:
                    return 1
    return 0

for _ in range(10):
    test_case = int(input())
    matrix = [list(map(int, input())) for _ in range(16)]

    start_x, start_y = 0, 0
    for i in range(16):
        for j in range(16):
            if matrix[i][j] == 2:
                start_x, start_y = i, j

    ans = dfs(start_x, start_y)
    print(f'#{test_case} {ans}')


# ※ 꼭 기억할 것
# 1. 범위 조건은 반드시 'and' — or를 쓰면 하나만 만족해도 통과되어 에러 발생
#    (0 <= nx < 16 and 0 <= ny < 16)
# 2. 방문 처리: 통로(0)를 1로 바꿔야 무한 루프 방지 (벽으로 막는 것이 아니라 발자국을 남기는 개념)
# 3. 입력 포맷 주의: T = int(input()) 없이 'for _ in range(10)' 후 루프 안에서 테스트 케이스 번호를 직접 받음
# 4. 재귀 반환값 처리: dfs()가 1을 들고 오면 즉시 1을 반환해야 탐색이 성공적으로 종료됨
# 5. DFS 개념: 분신(그림자)을 보내고 막히면 되돌아오는(Backtracking) 방식
