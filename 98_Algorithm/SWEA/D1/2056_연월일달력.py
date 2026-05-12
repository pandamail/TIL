# [D1] 2056. 연월일 달력 (D1 BOSS)
# YYYYMMDD 형식의 날짜 문자열이 유효한 날짜인지 검사한다.
# 유효하면 YYYY/MM/DD 형식으로, 유효하지 않으면 -1을 출력한다.
# 입력: T, 이후 T줄에 걸쳐 8자리 날짜 문자열
# 출력: #케이스번호 YYYY/MM/DD 또는 #케이스번호 -1

T = int(input())
days_in_month = [0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]

for total_case in range(1, T+1):
    S = input()
    year = S[0:4]
    month = S[4:6]
    day = S[6:8]

    m_int = int(month)
    d_int = int(day)

    if 1 <= m_int <= 12 and 1 <= d_int <= days_in_month[m_int]:
        print(f"#{total_case} {year}/{month}/{day}")
    else:
        print(f"#{total_case} -1")
