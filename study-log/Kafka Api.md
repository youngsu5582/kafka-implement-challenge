## Kafka Api

모든 카프카 요청은 API 호출이다.
70개 가량의 API 들이 각각 다른 목적을 위해 정의되어 있다.

### Api Versioning

- Request 는 `request_api_version` 헤더로 사용하고 있는 버전을 명시한다.
- Response 는 요청을 받은 API 버전을 지원해주면 동일한 API 버전을 반환한다.
- 각각 API 버전 관리는 독립적이다. (`Produce (Version: 10)` 과 `Fetch (Version: 10)` 는 관련없음)

=> API 버저닝을 통해 양방향 호환성이 가능해진다.
( 새 클라이언트 <-> 구 서버, 구 클라이언트 <-> 새 서버 )

### `ApiVersions` API

브로커가 지원하는 API 버전들을 반환  

- 응답에 `error_code` 로 2 바이트 반환
- 0은 성공 코드(NO_ERROR)
- 그 외는 에러 코드(EX: 35, UNSUPPORTED_VERSION)

---

- Kafka 프로토콜 API 와 Kafka 코어 API 는 다르다.
  (코어 API 는 프로토콜 API 를 래핑한 고차원 API)

- request

는 기존 프로토콜 요청과 동일하다.

[ApiVersions Request (v4) Spec](https://binspec.org/kafka-api-versions-request-v4)
[ApiVersions Response (v4)](https://binspec.org/kafka-api-versions-Response-v4)

- response

![image](https://darhcarwm16oo.cloudfront.net/1bb7ee6a1a47c9e9b78ee971841f4aa5.png)

  - api_keys
  - ![image](https://darhcarwm16oo.cloudfront.net/f312e52c2c930b4e499842e93b90f261.png)


### DescribeTopicPartitions API

특정 토픽의 파티션 상세 정보를 반환

- Partition ID : 토픽 내 몇 번 파티션
- Leader : 해당 파티션의 리더 (쓰기, 읽기 담당 브로커) 브로커
- Replicas : 복제본이 어느 브로커들에 분산되어 있는지
- ISR : In-Sync Replicas, 현재 리더와 데이터가 완전히 동기되어 있는 복제본 목록
- Offline Replicas : 장애등 무언가로 사용할 수 없는 복제본

=> 운영 및 관리 도구 만들때 사용