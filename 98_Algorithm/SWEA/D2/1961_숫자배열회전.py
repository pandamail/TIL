# [D2] 1961. 숫자 배열 회전
# N×N 행렬을 90도, 180도, 270도 회전시킨 결과를 한 줄에 나란히 출력한다.
# 핵심: zip(*arr[::-1]) — 행렬을 시계 방향으로 90도 회전시키는 파이썬 패턴
#   - arr[::-1]: 행 순서를 뒤집음
#   - zip(*...): 열을 행으로 전치
# 입력: T, 이후 각 테스트 케이스마다 N, N×N 행렬
# 출력: #케이스번호, 이후 N줄에 걸쳐 90도·180도·270도 회전 결과를 공백 없이 한 줄 출력

# ※ 미완성 — 출력 부분 디버깅 중

T = int(input())

for test_case in range(1, T + 1):
    N = int(input())
    matrix = []
    for _ in range(N):
        row = list(map(int, input().split()))
        matrix.append(row)

    def rotate_90(arr):
        return [list(row) for row in zip(*arr[::-1])]

    rotated_90 = rotate_90(matrix)
    rotated_180 = rotate_90(rotated_90)
    rotated_270 = rotate_90(rotated_180)

    print(f'#{test_case}')
    for i in range(N):
        row_90 = ''.join(map(str, rotated_90[i]))
        row_180 = ''.join(map(str, rotated_180[i]))
        row_270 = ''.join(map(str, rotated_270[i]))
        print(row_90, row_180, row_270)
