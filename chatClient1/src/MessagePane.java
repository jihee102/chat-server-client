import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

public class MessagePane extends JPanel implements MessageListener {
    private final ChatClient client;
    private String chatroomName;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();
    private JButton sendBtn = new JButton();

    public MessagePane(ChatClient client, String chatroomName, String theme ) {
        this.client = client;
        this.chatroomName = chatroomName;

        if(theme.equals("group")){
            client.addGroupMsgListener(chatroomName, this);
        }else if(theme.equals("private")){
            client.addPrivateMsgListener(chatroomName, this);
        }else {
            client.addBroadcastMsgListener(this);
        }


        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                if(theme.equals("group")){
                    client.sendGroupMessage(chatroomName, text);
                }else if(theme.equals("private")){
                    client.sendPrivateMessage(chatroomName, text);
                }else {
                    client.sendBroadMessage(text);
                }

                listModel.addElement("You: "+text);
                inputField.setText("");
            }
        });


    }


    @Override
    public void onMessage(String from, String message) {
        String line = from+": "+message;
        listModel.addElement(line);

    }
}
