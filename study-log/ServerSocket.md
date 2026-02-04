## ServerSocket

연결 요청이 들어올 때까지 실행 흐름을 멈추기 대기 - Block

-> 소켓에 대한 커넥션이 수립될 때까지 블로킹

- 소켓 타임아웃이 설정되어 있으면, 연결을 기다리다 설저된 시간이 지나면 `SocketTimeoutException` 던지며 해제


```java
public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");
        Socket s = new Socket((SocketImpl) null);
        implAccept(s);
        return s;
}
```

1. 소켓 닫혀있는지 확인 + 바운딩 되어있는지 확인

- ServerSocketChannel (NIO) 라면, 연결 수락중인 스레드에 인터럽트 발생시 기본 채널이 폐쇄, ClosedByInterruptException
- 시스템 기본 소켓 + Virtual Thread 가 연결 수락중이라면, 가상 스레드 인터럽트 발생시 스레드가 깨어남, SocketException

2. 새로운 Socket 객체 생성
3. 실제 연결 수락 처리, 소켓 객체에 연결 정보 추가

```java
private void implAccept(SocketImpl si) throws IOException {
    assert !(si instanceof DelegatingSocketImpl);

    // accept a connection
    try {
        impl.accept(si);
    } catch (SocketTimeoutException e) {
        throw e;
    } catch (InterruptedIOException e) {
        Thread thread = Thread.currentThread();
        if (thread.isVirtual() && thread.isInterrupted()) {
            close();
            throw new SocketException("Closed by interrupt");
        }
        throw e;
    }

    // check permission, close SocketImpl/connection if denied
    @SuppressWarnings("removal")
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        try {
            sm.checkAccept(si.getInetAddress().getHostAddress(), si.getPort());
        } catch (SecurityException se) {
            si.close();
            throw se;
        }
    }
}
```

- Security Manager 가 해당 작업 허용되는지 확인 (불가능하면 close)
- 이 시점에 실제 TCP Handshake 가 완료된 연결 정보가 소켓 객체에 주입 (시스템 레벨 네트워크 스택 - 자바 객체 연결 지점)

4. 새로운 Socket 객체 반환

