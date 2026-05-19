# [D2] 1966. 정렬
# N개의 정수를 입력받아 오름차순으로 정렬하여 출력한다.
# 입력: T, 이후 각 테스트 케이스마다 N, N개의 정수
# 출력: #케이스번호 정렬된 수열 (공백 구분)

# 핵심 Python 내장 함수 정리
# numbers.sort()          → 오름차순 정렬 (원본 변경)
# numbers.sort(reverse=True) → 내림차순 정렬
# sorted(numbers)         → 오름차순 정렬 새 리스트 반환
# max(numbers) / min(numbers) / sum(numbers)
# numbers.count(3)        → 3이 몇 개?
# numbers.index(5)        → 5의 위치?
# print(*numbers)         → 리스트를 공백 구분으로 언패킹 출력

T = int(input())

for test_case in range(1, T + 1):
    N = int(input())
    numbers = list(map(int, input().split()))
    numbers.sort()
    print(f'#{test_case}', *numbers)
