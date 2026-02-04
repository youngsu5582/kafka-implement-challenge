## Thread

CPU 자원을 점유하기 위한 논리적 단위

`Thread.sleep`, `socket.accept()` 와 같은 코드는 Blocking 을 한다.
메인 스레드에서 실행을 한다면..?
-> 하나의 요청을 처리하는 동안 다른 클라이언트는 대기를 해야 한다.

- eetop : 실제 `JavaThread` (C++ 객체) 주소 가르키는 포인터
- tid : Thread ID, 스레드 고유 식별자
- FieldHolder : 스레드 상태(priority, daemon, threadStatus 등) 관리

### Platform Thread

- 1:1 매핑, 자바 스레드 1개 - 운영체제 커널 스레드 1개
- 커널 스레드는 생성 비용이 크고, 메모리도 1MB 이상 점유
- 톰캣 같은 Thread-per-request 모델시, 접속자를 전부 받으면 OOM or Context Switching 등으로 한계가 발생한다.

### Virtual Thread

- M:N 매핑, 수백만 개 가상 스레드 - 소수 커널 스레드
- 기존 I/O 차단을 피하기 위한 Non-Blocking (CompletableFuture, WebFlux) 등
비동기 프로그래밍을 사용하지 않고, 동기식 코드로도 비동기적 퍼포먼스 가능

### 스레드 생명주기

- start : start0, Native Method, OS 에게 실행 권한 요청, 스레드는 `NEW` -> `RUNNABLE` 상태로 변하며 스케줄러 선택 대기
- exit : 스레드 종료시 호출, threadLocals clear

- 이제는 Thread + ThreadLocal 이 아닌, VirtualThread + ScopedValue 를 사용하는게 베스트
- 스레드는 외부에서 강제로 죽일수 없음 (stop 은 deprecated)

> 왜 deprecated?
> 스레드를 강제로 중단하면, 스레드가 잠궜던 모든 모니터 잠금 해제
> 모니터에 보호받던 객체의 일관성이 깨진 상태로, 다른 스레드에 노출 가능해짐. -> 불가능한 동작 초래
> (A 계좌에서 100원을 빼고 B 계좌에 100원을 넣는 스레드)
> (A 에서 돈을 뺀 상태로, stop 시 B에 돈이 들어가지 않은 상태로 락이 풀림, 일종의 트랜잭션인듯...)
> => 스레드에게 중단 요청 (interrupt) 하고, 스레드가 스스로 판단해서 종료

- `Thread.interrupted()` 를 통한 Graceful Shutdown 을 구현