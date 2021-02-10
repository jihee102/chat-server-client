import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GroupListPane extends JPanel implements GroupListener {
    private final ChatClient client;
    private JList<String> groupListUI;
    private DefaultListModel<String> groupListModel;
    private JTextField groupName;
    private JButton makeGroupBtn;

    public GroupListPane(ChatClient client) {
        this.client = client;

        client.addGroupListener(this);


        groupListModel = new DefaultListModel<>();
        groupListUI = new JList<>(groupListModel);
        setLayout(new BorderLayout());
        add(new JLabel("Group List"), BorderLayout.NORTH);


        add(new JScrollPane(groupListUI), BorderLayout.CENTER);
        groupName = new JTextField();

        makeGroupBtn = new JButton("Create Group");
        Font newBtnFont  = new Font(makeGroupBtn.getFont().getName(), makeGroupBtn.getFont().getStyle(), 10);
        makeGroupBtn.setFont(newBtnFont);


        JPanel inputPanel = new JPanel();
        GridBagLayout gridLayout = new GridBagLayout();
        inputPanel.setLayout(gridLayout);


        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx =0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.ipady= 10;
        inputPanel.add(groupName, c);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.gridx =1;
        c1.gridy = 0;
        c1.ipady= 10;
        inputPanel.add(makeGroupBtn, c1);


        add(inputPanel, BorderLayout.SOUTH);


        makeGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createGroup();
            }
        });



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

    private void createGroup() {
        if(groupName.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "Please type group name");
        }else{
            client.sendCreateGroup(groupName.getText());
        }
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
