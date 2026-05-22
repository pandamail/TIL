# [D3] 2805. 농작물 수확
# N×N 정사각형 밭에서 마름모 모양으로 색칠된 칸의 값을 모두 더한다.
# 마름모는 정중앙을 기준으로 위로 갈수록 좁아지고 아래로 갈수록 좁아지는 형태.
# 입력: T, 이후 각 테스트 케이스마다 N, N줄의 N자리 숫자 (공백 없이 붙어서 입력)
# 출력: #케이스번호 마름모 안 숫자 합

T = int(input())

for test_case in range(1, T + 1):
    N = int(input())
    matrix = [list(map(int, input())) for _ in range(N)]

    ans = 0
    center = N // 2
    start, end = center, center

    for i in range(N):
        ans += sum(matrix[i][start:end + 1])

        if i < center:
            start -= 1
            end += 1
        else:
            start += 1
            end -= 1

    print(f'#{test_case} {ans}')


# ※ 꼭 기억할 것
# 1. 공백 없이 붙은 숫자 입력: list(map(int, input())) — .split() 쓰면 통째로 묶여 대참사
# 2. center = N // 2 로 정중앙 인덱스를 잡고 start, end를 center로 초기화
# 3. 마름모 슬라이싱 규칙: i < center → 양옆 벌리기(start-=1, end+=1) / 이후 → 좁히기
# 4. 슬라이싱한 행의 합: sum(matrix[i][start:end+1]) — ans에 리스트를 직접 더하면 오류
