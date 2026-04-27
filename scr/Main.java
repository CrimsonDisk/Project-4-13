import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog("Enter Scale (1, 2, 3 or 4):");
        if (input == null) return;
        
        int scale = Integer.parseInt(input);


        JFrame frame = new JFrame("Mini 3D Tetris");
        GamePanel game = new GamePanel(scale);
        
        frame.add(game);
        frame.setResizable(false);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //---[The window scaling]---
        String scaleInput = JOptionPane.showInputDialog("Choose your window scale, 2 is recommended.");
        int winScale = 2;
        try { winScale = Integer.parseInt(scaleInput); }
        catch (NumberFormatException e) { winScale = 2; }

        //---[Title and going borderless]---
        JFrame newframe = new JFrame("Project 3-14 alpha0.4");
        newframe.setUndecorated(true);
        newframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //---[Game stuff and display size]---
        GamePanel tetrisGame = new GamePanel(winScale);
        newframe.add(tetrisGame);

        //---[Some other display]---
        newframe.pack();
        newframe.setLocationRelativeTo(null);
        newframe.setVisible(true);

    }
}