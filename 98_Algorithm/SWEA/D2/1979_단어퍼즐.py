# [D2] 1979. 어디에 단어가 들어갈 수 있을까
# N×N 퍼즐에서 길이가 정확히 K인 단어가 들어갈 수 있는 칸의 수를 구한다.
# 1이 연속으로 정확히 K개인 구간을 가로/세로 방향으로 탐색한다.
# 핵심: 연속 카운트 후 else 블록에서 리셋 → 줄 끝 처리 별도 체크 필수
# 입력: T, 이후 각 테스트 케이스마다 N K, N×N 행렬 (0 또는 1)
# 출력: #케이스번호 가능한 칸 수

T = int(input())

for test_case in range(1, T + 1):
    N, K = map(int, input().split())

    puzzle = []
    for _ in range(N):
        row = list(map(int, input().split()))
        puzzle.append(row)

    answer = 0

    # 가로 탐색
    for row in puzzle:
        count = 0
        for num in row:
            if num == 1:
                count += 1
            else:
                if count == K:
                    answer += 1
                count = 0
        if count == K:  # 행 끝 체크
            answer += 1

    # 세로 탐색
    for col in range(N):
        count = 0
        for row in range(N):
            num = puzzle[row][col]
            if num == 1:
                count += 1
            else:
                if count == K:
                    answer += 1
                count = 0
        if count == K:  # 열 끝 체크
            answer += 1

    print(f'#{test_case} {answer}')
