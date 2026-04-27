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
    private int cols = 7; //default is 13
    private int rows = 21;
    private int startCol = 2; //default is 6
    private int brickPixelHitBox = 13;
    
    private int setSpeedBase = 1;
    private int setSpeedFastFalling = 6;
    private int movePixelByFrame = 2;

    private int[][] brickboard = new int[rows][cols];

    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();

    private int screenWidth = 13 * cols;
    private int screenHeight = 13 * rows;

    private boolean isFastFalling = false;
    private int currentBrickIDColour = 1;
    private java.util.Random rand = new java.util.Random();

    //---[Brick math, positions and game window scaling, size, background]---
    private int brickX = brickPixelHitBox *  startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage[] brickTexture = new BufferedImage[6];

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
                int gridX = brickX / brickPixelHitBox;
                int gridYTop = brickY / brickPixelHitBox;
                int gridYBottom = (brickY + brickPixelHitBox - 1) / brickPixelHitBox;

                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    int nextGridX = gridX - 1;
                    if (nextGridX >= 0) {
                        if (brickboard[gridYTop][nextGridX] == 0 && brickboard[gridYBottom][nextGridX] == 0) {
                            brickX -= brickPixelHitBox;
                        }
                    }
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    int nextGridX = gridX + 1;
                    if (nextGridX < cols) {
                        if (brickboard[gridYTop][nextGridX] == 0 && brickboard[gridYBottom][nextGridX] == 0) {
                            brickX += brickPixelHitBox;
                        }
                    }
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

    //---[Full line check and pop, god I ####ing hate this part]---
    private void checkForFullRow() {
        for (int currentRow = rows - 1; currentRow > 0; currentRow--) {
            boolean rowIsFull = true;

            for (int currentCol = 0; currentCol < cols; currentCol++) {
                if (brickboard[currentRow][currentCol] == 0) {
                    rowIsFull = false;
                    break;
                }
            }

            if (rowIsFull) {
                for (int currentCol = 0; currentCol < cols; currentCol++) {
                    brickboard[currentRow][currentCol] = 0;
                }


            }






        }
    }

    //---[Loads brick textures]---
    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan"};

        try {
            for (int i = 0; i < colourNames.length; i++) {
                String path = "resources/textures/bricks/" + colourNames[i] + " Brick.png";
                brickTexture[i] = ImageIO.read(new File(path));
            }
        }
        catch (Exception e) { System.out.println("Cannot load, possibly missing texture: " + e.getMessage()); }
    }

    //---[The Runnable stuff. The brick movement and stop updates, and a somewhat stable 60 fps cap]---
    @Override
    public void run() {
        while(true) {
            background.updateAnimation();
            frameCounter++;

            if (frameCounter >= movePixelByFrame) {
                int currentSpeed = (isFastFalling == true) ? setSpeedFastFalling : setSpeedBase;

                for (int i = 0; i < currentSpeed; i++) {

                    int gridX = brickX / brickPixelHitBox;
                    int gridY = brickY / brickPixelHitBox;

                    boolean isBrickBelow = false;
                    if (gridY + 1 < rows) {
                        if (brickboard[gridY + 1][gridX] > 0) { isBrickBelow = true; }
                    }

                    //Falling and stopping checks
                    if (brickY < screenHeight - brickPixelHitBox && !isBrickBelow) { brickY++; }
                    else {
                        brickboard[gridY][gridX] = currentBrickIDColour;

                        checkForFullRow();

                        brickY = 0;
                        brickX = brickPixelHitBox * startCol;
                        isFastFalling = false;
                        currentBrickIDColour = rand.nextInt(6) + 1;
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

    //---[The Paint Component, the texturing stuff]---
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        background.drawGrid(g2, rows, cols, brickPixelHitBox, winScale);

        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                int colourID = brickboard[r][c];
                if (colourID > 0) {
                    g2.drawImage(brickTexture[colourID - 1], (c * brickPixelHitBox) * winScale, ((r * brickPixelHitBox) - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale,null);
                }
            }
        }

        if (brickTexture != null) {
            g2.drawImage(brickTexture[currentBrickIDColour - 1], brickX * winScale, (brickY - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
        }

    }

}