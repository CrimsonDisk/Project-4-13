import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements Runnable {
    //---[Main window and scaling stuff]---
    private int winScale;
    private int cols = 10;
    private int rows = 20;
    private int startCol = 5;
    private int brickPixel = 13;

    private int screenWidth = 13 * cols;
    private int screenHeight = 13 * rows;

    //---[Brick math, positions and game window scaling, size, background]---
    private int brickX = brickPixel *  startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage bufferedImage;

    public GamePanel(int winScale) {
        this.winScale = winScale;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);

        Thread gameThread = new Thread(this);
        gameThread.start();
    }











































}
