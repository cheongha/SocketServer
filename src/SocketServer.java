import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SocketServer {

    // 지금까지 한일. GUi연동시키면서 서버Gui에 메시지띄움.
    // 다음 이슈. Gui 상에서 일단 1:1 채팅을 하고 싶다.
    private ServerSocket serverSocket;
    private Socket socket;
    private String msg;

    /** XXX 03. 세번째 중요한것. 사용자들의 정보를 저장하는 맵입니다. */
    private Map<String, DataOutputStream> clientsMap = new HashMap<String, DataOutputStream>();

    public void setting() throws IOException {
        Collections.synchronizedMap(clientsMap); // 이걸 교통정리 해줍니다^^
        serverSocket = new ServerSocket(7788);
        while (true) {
            /** XXX 01. 첫번째. 서버가 할일 분담. 계속 접속받는것. */
            System.out.println("서버 대기중...");
            socket = serverSocket.accept(); // 먼저 서버가 할일은 계속 반복해서 사용자를 받는다.
            System.out.println(socket.getInetAddress() + "에서 접속했습니다.");
            // 여기서 새로운 사용자 쓰레드 클래스 생성해서 소켓정보를 넣어줘야겠죠?!
            Receiver receiver = new Receiver(socket);
            receiver.start();
        }
    }

    public static void main(String[] args) throws IOException {
        SocketServer serverBackground = new SocketServer();
        serverBackground.setting();
    }

    // 맵의내용(클라이언트) 저장과 삭제
    public void addClient(String nick, DataOutputStream out) throws IOException {
        sendMessage(nick + "님이 접속하셨습니다.\n");
        clientsMap.put(nick, out);
    }

    public void removeClient(String nick) {
        sendMessage(nick + "님이 나가셨습니다.\n");
        System.out.println(socket.getInetAddress() + "에서 접속을 끊었습니다.");

        clientsMap.remove(nick);
    }

    // 메시지 내용 전파
    public void sendMessage(String msg) {
        Iterator<String> it = clientsMap.keySet().iterator();
        String key = "";
        while (it.hasNext()) {
            key = it.next();
            try {
                clientsMap.get(key).writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // -----------------------------------------------------------------------------
    class Receiver extends Thread {
        private DataInputStream in;
        private DataOutputStream out;
        private String nick;

        /** XXX 2. 리시버가 한일은 자기 혼자서 네트워크 처리 계속..듣기.. 처리해주는 것. */
        public Receiver(Socket socket) throws IOException {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            nick = in.readUTF();
            addClient(nick, out);
        }

        public void run() {
            try {// 계속 듣기만!!
                while (in != null) {
                    msg = in.readUTF();
                    sendMessage(msg);
                    System.out.println(msg);
                    // gui.appendMsg(msg);
                }
            } catch (IOException e) {
                // 사용접속종료시 여기서 에러 발생. 그럼나간거에요.. 여기서 리무브 클라이언트 처리 해줍니다.
                System.out.println(nick + "님 나가셨습니다");
                removeClient(nick);
            }
        }
    }
}