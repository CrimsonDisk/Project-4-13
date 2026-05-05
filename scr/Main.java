import java.awt.Color;
import javax.swing.*;

public class Main {
    public static void main(String[] args){

        //---[The window scaling]---
        String scaleInput = JOptionPane.showInputDialog("Choose your window scale, 2 is recommended.");
        int winScale = 2;
        try { winScale = Integer.parseInt(scaleInput); }
        catch (NumberFormatException e) { winScale = 2; }

        //---[Title and going borderless]---
        JFrame gameframe = new JFrame("Project 3-14 beta3");
        gameframe.setUndecorated(true);
        gameframe.setBackground(new Color(0, 0, 0, 0));
        gameframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //---[Game stuff and display size]---
        Ui uiContainer = new Ui(winScale);
        gameframe.add(uiContainer);

        //---[Some other display]---
        gameframe.pack();
        gameframe.setLocationRelativeTo(null);
        gameframe.setVisible(true);
    }
}