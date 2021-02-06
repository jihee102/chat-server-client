import java.net.Socket;

public class PingPongWorker extends Thread{
    private boolean isPong;

    public PingPongWorker(ServerWorker serverWorker, Socket clientSocket) {
    }

    public void setPingPong(boolean isPong) {
        this.isPong = isPong;
    }
}
