import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String username = null;
    private PrintWriter writer;
    private OutputStream outputStream;
    private PingPongWorker pingPongWorker;
    private boolean heartBeat = false;
    private final ResponseMessages resMsg = new ResponseMessages();
    private final Protocols pt = new Protocols();
    private HashSet<String> groupSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server =server;
    }

    /**
     * run server worker thread
     */
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            System.out.println("Client disconnection notice: " + this.clientSocket);
            try {
                handleLogOff();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    /**
     * run the thread function
     * @throws IOException
     */
    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        outputStream = clientSocket.getOutputStream();
        writer = new PrintWriter(outputStream, true);

        // welcome message
        sendMsgToClientWithCode(pt.ok_200, resMsg.welcome);
        // run system
        usageLoop(reader);


    }

    /**
     * server protocol receiver loop
     * @param reader
     * @throws IOException
     */
    private void usageLoop(BufferedReader reader) throws IOException {

        String line;
        boolean disconnect = false;
        while ((line = reader.readLine()) != null && !disconnect) {
            String command = getResCode(line);
            String content = line.substring(line.indexOf(" ") + 1);
            String[] reqArr = strToStrArray(content);

            if ("CONN".equalsIgnoreCase(command)) {
                handleLogin(reqArr);
            } else if (username != null) {

                // functionality protocols
                switch (command) {
                    case "QUIT":
                        handleLogOff();
                        disconnect = true;
                        break;
                    case "PMSG":
                        handlePrivateMessage(reqArr);
                        break;
                    case "BCST":
                        sendBroadMessage(reqArr);
                        break;
                    case "CGROUP":
                        createGroup(reqArr);
                        break;
                    case "EXITGRP":
                        handleLeaveGroup(reqArr);
                        break;
                    case "GROUPMSG":
                        handleGroupMessage(reqArr);
                        break;
                    case "JOINGRP":
                        handleJoin(reqArr);
                        break;
                    case "PONG":
                        handlePong();
                        break;
                    default:
                        String msg = "unknown " + command + "/n";
                        sendMsgToClientWithCode(pt.badReq_400, msg);
                        break;
                }
            } else {
                String msg = "Please login first.";
                sendMsgToClientWithCode(pt.badReq_400, msg);
            }
        }
    }



    /**
     * user list
     */
    private void sendOnlineUserList() {
        List<ServerWorker> workers = server.getServerWorkerList();

        if (workers.size() == 1) {
            String msg = "You are the only one" + "\n";
            sendMsgToClientWithCode(pt.ok_200, msg);
            return;
        }

        // else
        String msg = "";
        for (ServerWorker worker : workers) {
            if (!username.equals(worker.username)) {
                msg += worker.getUsername() + pt.protDT;
            }
        }
        sendMsgToClientWithCode(pt.USERLIST, msg);

    }

    private void sendGroupList(){
        String msg = "";
        for(String group: server.getGroups()){
            msg+= group+pt.protDT;
        }
        sendMsgToClientWithCode(pt.GROUPLIST, msg);

    }

    /**
     * Private Message
     * @param arrOfStr: receiver message
     */
    private void handlePrivateMessage(String[] arrOfStr) {
        String username = arrOfStr[0];
        String msg = arrOfStr[1];
        ServerWorker worker = server.getServerWorkerByUsername(username);

        if (worker == null) {
            sendMsgToClientWithCode(pt.badReq_400, resMsg.userNotFound);
            return;
        }

        //else
        worker.sendMsgToClientWithCode(pt.PMSG, this.username + pt.protDT + msg);
        sendMsgToClientWithCode(pt.ok_200, resMsg.success);

    }

    /**
     * Create group
     * @param arrOfStr : groupName
     */
    private void createGroup(String[] arrOfStr) {
        String groupName = arrOfStr[0];
        if (groupName.length() <= 2) {
            sendMsgToClientWithCode(pt.badReq_400, resMsg.shouldbeMoreThanTwoLetters);
            return;
        }

        if (server.checkGroupExist(groupName)) {
            sendMsgToClientWithCode(pt.conflict_409, resMsg.groupAlreadyExist);
            return;
        }

        server.createNewGroup(groupName);
        groupSet.add(groupName);
        sendMsgToClientWithCode(pt.ok_200, resMsg.groupCreated);

        String msg = pt.CGROUP + " "+ groupName;
        sendMessageToAll(msg);
        startPingPong();

    }



    /**
     * Join group
     * @param arrOfStr : groupName
     */
    private void handleJoin(String[] arrOfStr) {
        String groupRoom = arrOfStr[0];

        // chk group exist
        if (!checkGroupExist(groupRoom)) {
            return;
        }

        // chk if user is in the group
        if (groupSet.contains(groupRoom)) {
            sendMsgToClientWithCode(pt.badReq_400, resMsg.memberInGroup);
            return;
        }

        groupSet.add(groupRoom);

        sendMsgToClientWithCode(pt.JOINGRP, resMsg.enteredGroup + ": " + groupRoom);

    }

    public boolean isMemberOfGroup(String group){
        return groupSet.contains(group);
    }

    /**
     * Group Message
     * @param arrOfStr
     */
    private void handleGroupMessage(String[] arrOfStr) {
        String groupRoom = arrOfStr[0];
        String msg = arrOfStr[1];

        // chk group exist
        if (!checkGroupExist(groupRoom)) {
            return;
        }

        if (!isMemberOfGroup(groupRoom)) {
            // if the user is not the member of the group
            sendMsgToClientWithCode(pt.unauthorized_401, resMsg.notInGroup);
            return;
        }

        String outMsg =  groupRoom+pt.protDT+username + pt.protDT + msg ;

        // send the message to the users in the group
        sendMessageToUsersInGroup(groupRoom,outMsg);

    }


    /**
     * Leave a group
     * @param arrOfStr: groupName
     */
    private void handleLeaveGroup(String[] arrOfStr) {
        String groupRoom = arrOfStr[0];

        // chk group exist
        if (!checkGroupExist(groupRoom)) {
            return;
        }

        if (!groupSet.contains(groupRoom)) {
            // if the user is not the member of the group
            sendMsgToClientWithCode(pt.unauthorized_401, resMsg.notInGroup);
            return;
        }

        groupSet.remove(groupRoom);
        sendMsgToClientWithCode(pt.ok_200, resMsg.leftGroup);
    }


    /**
     * send broadcast message
     * @param arrOfStr : message
     * @throws IOException
     */
    private void sendBroadMessage(String[] arrOfStr) {
        String msg = arrOfStr[0];
        String outMsg = pt.BCST + " " + this.username + pt.protDT + msg;
        sendMessageToUsers(outMsg);
    }


    /**
     * login user
     * @param arrOfStr
     */
    private void handleLogin(String[] arrOfStr) {
        String username = arrOfStr[0];
        boolean duplicatedName = false;

        // Check the username already exists.
        List<ServerWorker> workers = server.getServerWorkerList();
        for (ServerWorker worker : workers) {
            if (username.equals(worker.username)) {
                duplicatedName = true;
            }
        }

        if(duplicatedName){
            sendMsgToClientWithCode(pt.conflict_409, resMsg.usernameTaken);
            return;
        }

        if(username.length() <2){
            sendMsgToClientWithCode(pt.badReq_400, resMsg.usernameTooShort);
            return;
        }
        this.username = username;
        String msg = pt.ONLINE + " "+ username;
        sendMessageToUsers(msg);
        startPingPong();

        // after login
        // send current online user list
        sendOnlineUserList();

        // send current group list
        sendGroupList();

    }

    /**
     * send message to users except the current user
     * @param msg
     */
    public void sendMessageToUsers(String msg) {
        for (ServerWorker worker : server.getServerWorkerList()) {
            if (!this.getUsername().equalsIgnoreCase(worker.getUsername())) {
                worker.sendMsgToClient(msg);
            }
        }
        sendMsgToClientWithCode(pt.ok_200, resMsg.success);
    }
    /**
     * send message to users in the particular group
     * @param msg
     */
    public void sendMessageToUsersInGroup(String group,String msg) {
        for (ServerWorker worker : server.getServerWorkerList()) {
                if(!this.getUsername().equalsIgnoreCase(worker.getUsername())&&worker.isMemberOfGroup(group)){
                    worker.sendMsgToClientWithCode(pt.GROUPMSG , msg);
                }
        }
        sendMsgToClientWithCode(pt.ok_200, resMsg.success);
    }
    /**
     * send message to users including the current user
     * @param msg
     */
    public void sendMessageToAll(String msg) {
        for (ServerWorker worker : server.getServerWorkerList()) {
            worker.sendMsgToClient(msg);
        }
        sendMsgToClientWithCode(pt.ok_200, resMsg.success);
    }

    /**
     * check if group exist
     * @param groupName
     * @return
     */
    private boolean checkGroupExist(String groupName) {
        if (!server.checkGroupExist(groupName)) {
            sendMsgToClientWithCode(pt.notFound_404, resMsg.groupDoesNotExist);
            return false;
        }
        return true;
    }


    /**
     * start ping pong
     */
    private void startPingPong() {

        if (heartBeat) {
            pingPongWorker = new PingPongWorker(this, clientSocket);
            pingPongWorker.start();
        }
    }

    private void handlePong() {
        pingPongWorker.setPingPong(true);
    }

    /**
     * get response code from response
     * @param response
     * @return
     */
    public String getResCode(String response) {
        String[] arrOfStr = response.split(" ");
        return arrOfStr[0];
    }


    /**
     * string to array
     * @param string
     * @return
     */
    private String[] strToStrArray(String string) {
        return string.split(pt.protDT);
    }

    /**
     * disconnect client
     */
    private void handleLogOff() throws  IOException{
        server.removeWorker(this);
        String msg = pt.OFFLINE+ " "+ username;
        sendMessageToUsers(msg);
        clientSocket.close();
    }


    public String getUsername() {
        return username;
    }


    public void sendMsgToClientWithCode(String code, String message) {
        writer.println(code + " " + message);
    }

    public void sendMsgToClient( String message) {
        writer.println( message);
    }

}
