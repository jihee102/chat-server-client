import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow {
    private final ChatClient client;
    private JTextField usernameText;
    private JButton loginBtn;
    private JPanel panel;

    public LoginWindow(ChatClient client) {
        this.client = client;


        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.login(usernameText.getText());

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
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


        JFrame frame = new JFrame("Login");
        frame.setContentPane(new LoginWindow(client).panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
