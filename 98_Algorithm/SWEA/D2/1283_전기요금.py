# [D2] 1283. 단계별로 전진
# 두 가지 전기요금 방식 중 더 저렴한 요금을 출력한다.
# 방식A (단가 방식): 사용량 W × 단가 P
# 방식B (약정 방식): 기본료 Q, 사용량이 기본량 R 초과 시 Q + (W-R) × 초과단가 S
# 입력: T, 이후 T줄에 걸쳐 P Q R S W
# 출력: #케이스번호 최솟값

T = int(input())
for t in range(1, T+1):
    P, Q, R, S, W = map(int, input().split())
    cost_a = W * P
    if W <= R:
        cost_b = Q
    else:
        cost_b = Q + (W - R) * S
    result = min(cost_a, cost_b)
    print(f"#{t} {result}")
