import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    private int port;
    private ArrayList<ServerWorker> serverWorkerList = new ArrayList<>();
    private HashSet<String> groups = new HashSet<>();

    private HashMap<String, List<String>> files = new HashMap<>();

    public Server(int port){
        this.port = port;
    }
    /**
     * run server thread
     */
    @Override
    public void run() {
        try {
            // create server socket and client threads
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Ready to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                serverWorkerList.add(serverWorker);
                serverWorker.start();
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * get all user threads in  ArrayList ServerWorker
     *
     * @return
     */
    public ArrayList<ServerWorker> getServerWorkerList() {
        return serverWorkerList;
    }

    /**
     * get user thread by username
     *
     * @param username
     * @return
     */
    public ServerWorker getServerWorkerByUsername(String username) {

        for (ServerWorker worker : getServerWorkerList()) {
            if (worker.getUsername().equalsIgnoreCase(username)) {
                return worker;
            }
        }
        return null;
    }


    public void removeWorker(ServerWorker serverWorker) {
        // remove thread
        serverWorkerList.remove(serverWorker);
    }


    /**
     * check if group exist
     *
     * @param groupName
     * @return
     */
    public boolean checkGroupExist(String groupName) {
        if (groups.contains(groupName)) {
            return true;
        }
        return false;
    }


    /**
     * create new chat group
     *
     * @param groupName
     */
    public void createNewGroup(String groupName) {
        groups.add(groupName);
    }


    public HashSet<String> getGroups() {
        return groups;
    }
}
