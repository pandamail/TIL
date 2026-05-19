# [D2] 1928. Base64 Decoder
# Base64로 인코딩된 문자열을 디코딩하여 원본 문자열을 출력한다.
# 입력: T, 이후 각 테스트 케이스마다 Base64 인코딩 문자열
# 출력: #케이스번호 디코딩된 문자열

# ※ 보류 — base64 라이브러리 사용 방식으로 풀었으나 SWEA 제출 불가.
#   라이브러리 없이 직접 디코딩 로직 구현 필요. 추후 재도전 예정.

import base64

T = int(input())

for test_case in range(1, T + 1):
    S = input()
    S_decoding = base64.b64decode(S)
    S_str = S_decoding.decode('utf-8')
    print(f'#{test_case} {S_str}')
