import java.awt.Color;
import javax.swing.*;

public class Main {
    public static void main(String[] args){

        // --- [The player manager] ---
        PlayerManager manager = new PlayerManager();
        Player selected = manager.selectPlayer();

        // --- [The window scaling] ---
        String scaleInput = JOptionPane.showInputDialog("Choose your window scale, 2 is recommended.");
        int winScale = 2;
        try { winScale = Integer.parseInt(scaleInput); }
        catch (NumberFormatException e) { winScale = 2; }

        // --- [Title and going borderless] ---
        JFrame gameframe = new JFrame("Project 3-14 beta4.1");
        gameframe.setUndecorated(true);
        gameframe.setBackground(new Color(0, 0, 0, 0));
        gameframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- [Game display stuff] ---
        Ui uiContainer = new Ui(winScale, selected, manager);
        gameframe.add(uiContainer);

        gameframe.pack();
        gameframe.setLocationRelativeTo(null);
        gameframe.setVisible(true);
    }
}