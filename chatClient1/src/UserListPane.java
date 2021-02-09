import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {
    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();


        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JLabel("Online User List"), BorderLayout.NORTH);
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>1){
                    String user = userListUI.getSelectedValue();
                    MessagePane messagePane ;
                    if(client.getMessagePaneForPrivate(user)!=null){
                        messagePane = (MessagePane) client.getMessagePaneForPrivate(user);
                    }else{
                        messagePane = new MessagePane(client, user, "private");
                    }
                    JFrame frame = new JFrame("Chat with "+ user);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(500,500);
                    frame.getContentPane().add(messagePane, BorderLayout.CENTER);
                    frame.setVisible(true);

                }
            }
        });
    }


    @Override
    public void online(String user) {
        userListModel.addElement(user);
    }

    @Override
    public void offline(String user) {
        userListModel.removeElement(user);
    }
}
