import javax.swing.*;
import java.awt.*;
import java.awt.font.GraphicAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements Runnable {
    //---[Main window and scaling stuff]---
    private int winScale;
    private int cols = 13;
    private int rows = 21;
    private int startCol = 6;
    private int brickPixelHitBox = 13;
    
    private int setSpeedBase = 1;
    private int setSpeedFastFalling = 6;
    private int movePixelByFrame = 2;

    private int[][] brickboard = new int[rows][cols];

    private Bgm musicPlayer = new Bgm();

    private int screenWidth = 13 * cols;
    private int screenHeight = 13 * rows;

    private boolean isFastFalling = false;

    //---[Brick math, positions and game window scaling, size, background]---
    private int brickX = brickPixelHitBox *  startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage brickImg;

    public GamePanel(int winScale) {
        this.winScale = winScale;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");

        Thread gameThread = new Thread(this);
        gameThread.start();

        //---[Game controls, key listener thingy]---
        this.setFocusable(true);
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    if (brickX > 0) { brickX -= brickPixelHitBox; }
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    if (brickX < screenWidth - brickPixelHitBox) { brickX += brickPixelHitBox; }
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) System.exit(0);
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) { isFastFalling = true; }
            }
            
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) { isFastFalling = false; }
            }
        });
    }

    //---[Loads brick textures]---
    private void loadTexture() {
        try { brickImg = ImageIO.read(new File("resources/textures/bricks/Purple Brick.png")); }
        catch (Exception e) { System.out.println("Cannot load, possibly missing texture: " + e.getMessage()); }
    }

    //---[The Runnable stuff. The brick movement and stop updates, and a somewhat stable 60 fps cap]---
    @Override
    public void run() {
        while(true) {
            frameCounter++;

            if (frameCounter >= movePixelByFrame) {
                int currentSpeed = (isFastFalling == true) ? setSpeedFastFalling : setSpeedBase;

                for (int i = 0; i < currentSpeed; i++) {

                    int gridX = brickX / brickPixelHitBox;
                    int gridY = brickY / brickPixelHitBox;

                    boolean isBrickBelow = false;
                    if (gridY + 1 < rows) {
                        if (brickboard[gridY + 1][gridX] == 1) { isBrickBelow = true; }
                    }

                    //Falling and stopping checks
                    if (brickY < screenHeight - brickPixelHitBox && !isBrickBelow) { brickY++; }
                    else {
                        brickboard[gridY][gridX] = 1;

                        brickY = 0;
                        brickX = brickPixelHitBox * startCol;
                        isFastFalling = false;
                        break;
                    }
                }
                frameCounter = 0;
            }
            repaint();
            //FPS cap
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    //---[The texturing stuff]---
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] == 1) {
                    g2.drawImage(brickImg, (c * brickPixelHitBox) * winScale, ((r * brickPixelHitBox) - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale,null);
                }
            }
        }

        if (brickImg != null) {
            g2.drawImage(brickImg, brickX * winScale, (brickY - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
        }

    }






}
