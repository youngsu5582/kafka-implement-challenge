## Send Correlation ID

## Kafka Wire Protocol

- 요청-응답 모델

client 는 request message 를 전송 <-> 브로커는 response message 를 응답

세 개의 영역으로 구분되어 있다.

- 메시지 크기(`message_size`)
- Header
- Body

### message_size

- 32bit signed integer
- header 와 body 의 크기를 지정

> kafka 의 모든 숫자들은 big-endian 방식을 따른다.

```
00 00 00 02
```

메시지의 크기가 2를 의미한다.

### Correlation Identifier

- 분산 시스템에서 요청, 응답을 요청하기 위해 사용하는 패턴
- 전역적으로 고유한 식별자를 사용해 관련 이벤트들을 연결

![correlation-identifier](https://developer.confluent.io/408a9fcd8118b2e8d28aece8acf40dd9/correlation-identifier.svg)

```java
ProducerRecord<String, String> requestEvent = new ProducerRecord<>("request-event-key", "request-event-value"); 
requestEvent.headers().add("requestID", UUID.randomUUID().toString());
requestEvent.send(producerRecord);
```

요청에서 UUID 를 담아서 전송

```java
ProducerRecord<String, String> responseEvent = new ProducerRecord<>("response-event-key", "response-event-value"); 
responseEvent.headers().add("requestID", requestEvent.headers().lastHeader("requestID").value());
responseEvent.send(producerRecord);
```

응답에서 UUID 를 추출해서 사용
