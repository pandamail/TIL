# [D1] 1936. 가위바위보
# A와 B가 낸 손 모양을 입력받아 이긴 사람을 출력한다.
# 손 모양: 1=가위, 2=바위, 3=보
# A가 이기는 경우: (가위vs보), (바위vs가위), (보vs바위)
# 입력: 정수 A B (1 ≤ A, B ≤ 3)
# 출력: 이긴 사람 "A" 또는 "B" (비기는 경우는 없음)

A, B = map(int, input().split())
if (A == 1 and B == 3) or (A == 2 and B == 1) or (A == 3 and B == 2):
    print("A")
else:
    print("B")
