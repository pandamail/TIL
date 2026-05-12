# [D1] 6326. 팩토리얼
# 정수 N을 입력받아 N! (N 팩토리얼) 값을 출력한다.
# 입력: 정수 N (0 ≤ N ≤ 12)
# 출력: N!

def factorial(n):
    result = 1
    for i in range(1, n+1):
        result *= i
    return result

n = int(input())
print(factorial(n))
