
## Kafka Message Header

header 는 v1, v2 가 존재한다.

> 두개의 차이는 `_tagged_fields` 라는 있냐, 없냐의 차이.
> v2 에서 새로 추가되었다.

- `request_api_key` : INT 16, 2 byte, 요청을 위한 API KEY
- `request_api_version` : INT 16, 2 byte, 요청을 위한 API Version
- `correlation_id` : INT 32, 4 byte, 요청의 고유 식별자
- `client_id` : NULLABLE STRING, Variable, 요청을 위한 Client ID
- `TAG_BUFFER` : TAGGED_FIELDS, Variable, 추가 정보 넣기 위한 값, Optional

