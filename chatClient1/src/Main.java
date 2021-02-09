import javax.swing.*;

public class Main {

    public static void main(String[] args) {
	// write your code here
        ChatClient client = new ChatClient("localhost", 1337);

        if(!client.connect()){
            JOptionPane.showMessageDialog(null,"Connection failed.");
            System.err.println("Connection failed.");
        }else {
            JOptionPane.showMessageDialog(null,"Connection successful");
            System.out.println("Connection successful");
        }


    }
}
