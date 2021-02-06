import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GroupListPane extends JPanel implements GroupListener {
    private final ChatClient client;
    private JList<String> groupListUI;
    private DefaultListModel<String> groupListModel;

    public GroupListPane(ChatClient client) {
        this.client = client;

        client.addGroupListener(this);


        groupListModel = new DefaultListModel<>();
        groupListUI = new JList<>(groupListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(groupListUI), BorderLayout.CENTER);

        groupListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>1){
                    String group = groupListUI.getSelectedValue();
                    MessagePane messagePane;
                    if(client.getMessagePaneForGroup(group)!= null){
                        messagePane = (MessagePane) client.getMessagePaneForGroup(group);
                    }else{
                        messagePane = new MessagePane(client, group, "group");
                        client.sendJoinGroup(group);
                    }
                    JFrame frame = new JFrame("Group Chat - "+group );
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(500,500);
                    frame.getContentPane().add(messagePane, BorderLayout.CENTER);
                    frame.setVisible(true);

                }
            }
        });
    }


    @Override
    public void addGroup(String groupName) {
        groupListModel.addElement(groupName);
    }

    @Override
    public void removeGroup(String groupName) {
        groupListModel.removeElement(groupName);
    }
}
