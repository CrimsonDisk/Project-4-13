import javax.swing.*;

public class Main {
    public static void main(String[] args){

        //---[The window scaling]---
        String scaleInput = JOptionPane.showInputDialog("Choose your window scale, 2 is recommended.");
        int winScale = 2;
        try { winScale = Integer.parseInt(scaleInput); }
        catch (NumberFormatException e) { winScale = 2; }

        //---[Title and going borderless]---
        JFrame newframe = new JFrame("Project 3-14 alpha0.3.1");
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