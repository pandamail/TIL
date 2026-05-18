# [D2] 1959. 두 개의 숫자열
# 길이가 다른 두 수열 A, B가 주어질 때, 짧은 수열을 긴 수열 위에서 슬라이딩하며
# 같은 위치 원소끼리 곱한 합의 최댓값을 구한다.
# 핵심: 짧은 쪽을 고정하고 긴 쪽을 슬라이딩
#   - 슬라이딩 횟수: len(long) - len(short) + 1
#   - 각 위치 합: short[j] * long[i + j]
# 입력: T, 이후 각 테스트 케이스마다 N M, 수열 A(N개), 수열 B(M개)
# 출력: #케이스번호 최댓값

T = int(input())

for test_case in range(1, T + 1):
    N, M = map(int, input().split())
    Ai = list(map(int, input().split()))
    Bj = list(map(int, input().split()))

    if N < M:
        short = Ai
        long = Bj
    else:
        short = Bj
        long = Ai

    sliding_count = len(long) - len(short) + 1
    max_sum = -999

    for i in range(sliding_count):
        current_sum = 0
        for j in range(len(short)):
            current_sum += short[j] * long[i + j]
        if current_sum > max_sum:
            max_sum = current_sum

    print(f'#{test_case} {max_sum}')
