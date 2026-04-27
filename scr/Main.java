import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        
        int scale = 2; 
        String input = JOptionPane.showInputDialog(null, "Choose your window scale, 2 is recommended.");
        
        try {
            if (input != null && !input.isEmpty()) {
                scale = Integer.parseInt(input);
            } else if (input == null) {
                
                System.exit(0);
            }
        } catch (NumberFormatException e) {
            
            JOptionPane.showMessageDialog(null, "Invalid input! Using default scale 2.");
            scale = 2;
        }

        
        JFrame window = new JFrame();
        
        
        window.setTitle("Project 4-13 alpha 0.5.1");
        
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        
        GamePanel gamePanel = new GamePanel(scale);
        
        window.add(gamePanel);
        window.pack(); 

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        
        gamePanel.startGameThread();
    }
}