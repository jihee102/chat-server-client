import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;


public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private boolean login;
    private String username;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bReader;
    private PrintWriter pWriter;
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<GroupListener> groupListeners = new ArrayList<>();

    private ArrayList<MessageListener> broadcastMsgListeners = new ArrayList<>();

    private HashMap<String,MessageListener > privateMsgListeners = new HashMap<>();
    private HashMap<String,MessageListener > groupMsgListeners = new HashMap<>();

    private Protocols pt = new Protocols();

    /**
     * create instance of chat client
     *
     * @param clientName
     * @param serverPort
     */
    public ChatClient(String clientName, int serverPort) {
        this.serverName = clientName;
        this.serverPort = serverPort;
    }
    /**
     * main function
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        ChatClient client = new ChatClient("localhost", 1337);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String user) {
                System.out.println("ONLINE: "+ user);
            }

            @Override
            public void offline(String user) {
                System.out.println("OFFLINE: "+ user);

            }
        });

        client.addBroadcastMsgListener(new MessageListener() {
            @Override
            public void onMessage(String from, String message) {
                System.out.println("Broadcast Message from "+ from+"==>"+ message);
            }
        });

        if(!client.connect()){
            System.err.println("Connection failed.");
        }else {
            System.out.println("Connection successful");
        }

        if(!client.login("Jihee")){
            System.err.println("login failed.");
        }else{
            System.out.println("login successful");

        }


    }

    /**
     * connect with server
     *
     * @return
     */
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            this.serverIn = socket.getInputStream();
            this.serverOut = socket.getOutputStream();
            this.bReader = new BufferedReader(new InputStreamReader(serverIn));
            this.pWriter = new PrintWriter(serverOut, true);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username) throws IOException {
        sendMsgToServerWithCode(pt.CONN, username);

        String response = bReader.readLine();

        // check response protocol code
        String[] resArr = response.split(" ");
        if(resArr[0].equals("200")){
            startMessageReader();
            this.username = username;
            return true;
        }
        return false;
    }

    private void startMessageReader() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                readMessage();
            }
        };
        thread.start();
    }

    public void readMessage(){
        String response;

        try{
            while ((response = bReader.readLine()) != null){
                String command = getResCode(response);
                String content = response.substring(response.indexOf(" ") + 1);
                String[] reqArr = content.split(pt.protDT);

                switch (command){
                    case "USERLIST":
                        loadOnlineUsers(reqArr);
                        break;
                    case "GROUPLIST":
                        loadGroups(reqArr);
                        break;
                    case "ONLINE":
                        handleOnlineUser(reqArr);
                        break;
                    case "OFFLINE":
                        handleOfflineUser(reqArr);
                        break;
                    case "PMSG":
                        handlePrivateMsg(reqArr);
                        break;
                    case "BCST":
                        handleBroadcastMsg(reqArr);
                        break;
                    case "GROUPMSG":
                        handleGroupMsg(reqArr);
                        break;
                    case "CGROUP":
                        handleCreatedGroupMsg(reqArr);
                        break;
                    default:
                        System.out.println(response);
                }
            }


        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadGroups(String[] reqArr) {

        // if groupListeners is not added, which means the the panel is not loaded yet, then wait 100ms.
        if (groupListeners.size() ==0 && reqArr.length!=0){
            try {
                Thread.sleep(1 * 100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        for (String group: reqArr){
            for(GroupListener groupListener : groupListeners){
                groupListener.addGroup(group);
            }
        }
    }

    private void loadOnlineUsers(String[] reqArr) {

        // if userStatusListener is not added, which means the the panel is not loaded yet, then wait 100ms.
        if (userStatusListeners.size() ==0 && reqArr.length!=0){
            try {
                Thread.sleep(1 * 100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        for (String user : reqArr){
            for(UserStatusListener listener: userStatusListeners){
                listener.online(user);
            }
        }
    }

    private void handleCreatedGroupMsg(String[] reqArr) {
        String groupName = reqArr[0];
        for(GroupListener groupListener : groupListeners){
            groupListener.addGroup(groupName);
        }
    }

    private void handleGroupMsg(String[] reqArr) {
        String group = reqArr[0];
        String from = reqArr[1];
        String message = reqArr[2];

        if(!groupMsgListeners.containsKey(group)){
            MessagePane messagePane = new MessagePane(this, group, "group");
            JFrame frame = new JFrame("Chat with "+ group);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500,500);
            frame.getContentPane().add(messagePane, BorderLayout.CENTER);
            frame.setVisible(true);
            messagePane.onMessage(from,message);
            groupMsgListeners.put(group, messagePane);
        }else{
            groupMsgListeners.get(group).onMessage(from, message);
        }
    }

    private void handleBroadcastMsg(String[] reqArr) {
        String from = reqArr[0];
        String message = reqArr[1];

        for (MessageListener listener: broadcastMsgListeners){
            listener.onMessage(from, message);
        }

    }

    private void handlePrivateMsg(String[] reqArr) {
        String from = reqArr[0];
        String message = reqArr[1];

        if(!privateMsgListeners.containsKey(from)){
            MessagePane messagePane = new MessagePane(this, from, "private");
            JFrame frame = new JFrame("Chat with "+ from);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500,500);
            frame.getContentPane().add(messagePane, BorderLayout.CENTER);
            frame.setVisible(true);
            messagePane.onMessage(from, message);
            privateMsgListeners.put(from, messagePane);

        }else{
            privateMsgListeners.get(from).onMessage(from, message);
        }

    }

    public MessageListener getMessagePaneForGroup(String groupName){
        if(groupMsgListeners.containsKey(groupName)){
            return groupMsgListeners.get(groupName);
        }
        return null;
    }

    public MessageListener getMessagePaneForPrivate(String username){
        if(privateMsgListeners.containsKey(username)){
            return privateMsgListeners.get(username);
        }
        return null;
    }

    public void sendPrivateMessage(String sendTo, String message){
        String msg = sendTo+pt.protDT+message;
        sendMsgToServerWithCode(pt.PMSG, msg);
    }

    public void sendBroadMessage(String message){
        sendMsgToServerWithCode(pt.BCST, message);
    }

    public void sendGroupMessage(String group, String message){
        String msg = group+pt.protDT+message;
        sendMsgToServerWithCode(pt.GROUPMSG, msg);
    }

    public void sendCreateGroup(String groupName){
        sendMsgToServerWithCode(pt.CGROUP, groupName);
    }

    public void sendJoinGroup(String group){
        sendMsgToServerWithCode(pt.JOINGRP, group);
    }

    public void sendExitGroup(String group){
        sendMsgToServerWithCode(pt.EXITGRP, group);
    }

    private void handleOnlineUser(String[] reqArr) {
        String user = reqArr[0];
        for(UserStatusListener listener: userStatusListeners){
            listener.online(user);
        }
    }

    private void handleOfflineUser(String[] reqArr) {
        String user = reqArr[0];
        for(UserStatusListener listener: userStatusListeners){
            listener.offline(user);
        }
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addGroupListener(GroupListener groupListener){
        groupListeners.add(groupListener);
    }

    public void removeGroupListener(GroupListener groupListener){
        groupListeners.remove(groupListener);
    }






    public void addBroadcastMsgListener(MessageListener listener){
        broadcastMsgListeners.add(listener);
    }

    public void removeBroadcastMsgListener(MessageListener listener){
        broadcastMsgListeners.remove(listener);
    }

    public void addPrivateMsgListener(String user, MessageListener listener){
        privateMsgListeners.put(user, listener);
    }

    public void removePrivateMsgListener(String user){
        privateMsgListeners.remove(user);
    }

    public void addGroupMsgListener(String group, MessageListener listener){
        groupMsgListeners.put(group, listener);
    }

    public void removeGroupMsgListener(String group){
        groupMsgListeners.remove(group);
    }

    private void sendMsgToServerWithCode(String code, String message) {
        pWriter.println(code + " " + message);
    }

    /**
     * get response code from response
     *
     * @param response
     * @return
     */
    public String getResCode(String response) {
        String[] arrOfStr = response.split(" ");
        return arrOfStr[0];
    }

}
