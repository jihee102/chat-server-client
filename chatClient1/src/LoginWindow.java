import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame{
    private final ChatClient client;
    private JTextField usernameText;
    private JButton loginBtn;
    private JPanel panel;

    public LoginWindow(ChatClient client) {
        this.client = client;


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500,500);
        add(panel);


        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    private void doLogin() {
        try {
            client.login(usernameText.getText());

        } catch (IOException ioException) {
            ioException.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid login/password");
        } finally {
            setVisible(false);
            makeGUI();

        }
    }

    private void makeGUI(){
        JFrame frame = new JFrame("Main Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,800);

        UserListPane listPane = new UserListPane(client);
        GroupListPane groupListPane = new GroupListPane(client);

        GridBagLayout gridLayout = new GridBagLayout();
        frame.setLayout(gridLayout);


        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx =0;
        c.gridy = 1;
        c.gridwidth=2;
        c.ipady= frame.getHeight()/2-100;
        frame.add(new MessagePane(client, "Broad Chat", "Broad"), c);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.gridx =0;
        c1.gridy = 0;
        c1.weightx = 0.5;
        c1.ipady= frame.getHeight()/2-100;
        frame.add(listPane, c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx =1;
        c2.gridy = 0;
        c2.weightx = 0.5;
        c2.ipady= frame.getHeight()/2-100 -30;
        frame.add(groupListPane, c2);

        frame.setVisible(true);


    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 1337);

        if(!client.connect()){
            JOptionPane.showMessageDialog(null,"Connection failed.");
            System.err.println("Connection failed.");
        }else {
            JOptionPane.showMessageDialog(null,"Connection successful");
            System.out.println("Connection successful");
        }

        LoginWindow loginWindow = new LoginWindow(client);
        loginWindow.setVisible(true);

    }

}
