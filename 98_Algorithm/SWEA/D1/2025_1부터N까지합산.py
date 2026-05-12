# [D1] 2025. 1~N까지 합산
# 정수 N을 입력받아 1부터 N까지 모든 정수의 합을 출력한다.
# 입력: 정수 N (1 ≤ N ≤ 100)
# 출력: 1 + 2 + ... + N의 합

N = int(input())
total = 0
for i in range(1, N + 1):
    total += i
print(total)
