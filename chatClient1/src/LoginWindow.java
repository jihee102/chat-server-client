import javax.swing.*;
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
        UserListPane listPane = new UserListPane(client);
        listPane.setBounds(0,0, 250, 250);

        GroupListPane groupListPane = new GroupListPane(client);
        groupListPane.setBounds(250,0,250,250);


        JFrame frame = new JFrame("Main Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setSize(750,750);

        
        frame.add(listPane);
        frame.add(groupListPane);
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
