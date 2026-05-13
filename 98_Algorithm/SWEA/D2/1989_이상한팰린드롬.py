# [D2] 1989. 이상한 문자열 팰린드롬 검사
# 문자열을 입력받아 거꾸로 읽어도 같은 문자열(팰린드롬)이면 1, 아니면 0을 출력한다.
# 예) "level" → 1, "hello" → 0
# 핵심: 파이썬 슬라이싱 S[::-1]로 역순 문자열 생성
# 입력: T, 이후 T줄에 걸쳐 문자열
# 출력: #케이스번호 1 또는 #케이스번호 0

T = int(input().strip())

for test_case in range(1, T + 1):
    S = input()
    if S == S[::-1]:
        print(f"#{test_case} 1")
    else:
        print(f"#{test_case} 0")
