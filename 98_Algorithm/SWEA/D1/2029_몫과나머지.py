# [D1] 2029. 몫과 나머지
# T개의 테스트 케이스에 대해 a를 b로 나눈 몫과 나머지를 출력한다.
# 입력: T, 이후 T줄에 걸쳐 a b
# 출력: #케이스번호 몫 나머지

T = int(input())
for test_case in range(1, T+1):
    a, b = map(int, input().split())
    print(f"#{test_case}", a//b, a%b)
