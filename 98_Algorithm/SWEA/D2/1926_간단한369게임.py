# [D2] 1926. 간단한 369게임
# 1부터 N까지 순서대로 숫자를 말하되, 3·6·9가 포함된 숫자는 해당 자릿수만큼 "-"를 출력한다.
# 예) 3 → "-", 33 → "--", 36 → "--"
# 입력: 정수 N
# 출력: 1부터 N까지 공백 구분 출력 (3·6·9 포함 시 "-" 대체)

N = int(input())
for i in range(1, N + 1):
    s = str(i)
    clap = s.count('3') + s.count('6') + s.count('9')

    if clap > 0:
        print('-' * clap, end=' ')
    else:
        print(s, end=' ')
