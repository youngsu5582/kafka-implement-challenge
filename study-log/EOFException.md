## EOFException

End of File 을 만나면 발생하는 예외

- InputStream 에서 더 이상 읽을 데이터가 없는 경우
- Stream 이 EOF 문자를 발견하고, 이를 바탕으로 데이터 더 이상 존재하지 않는걸 감지

흔히, 우리가 Ctrl + C 를 입력하면 프로세스를 종료하며 열린 소켓 연결을 닫는다. - FIN 패킷 전송
-> 서버 측의 Stream 은 더 이상 읽을 데이터가 없다는 신호를 받음