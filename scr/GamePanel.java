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
    private int brickPixel = 13;

    private int screenWidth = 13 * cols;
    private int screenHeight = 13 * rows;

    //---[Brick math, positions and game window scaling, size, background]---
    private int brickX = brickPixel *  startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage brickImg;

    public GamePanel(int winScale) {
        this.winScale = winScale;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);

        loadTexture();
        playBgMusic();

        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    //---[Loads brick textures]---
    private void loadTexture() {
        try { brickImg = ImageIO.read(new File("resources/textures/bricks/Purple Brick.png")); }
        catch (Exception e) { System.out.println("Cannot load, possibly missing texture: " + e.getMessage()); }
    }

    //---[The Runnable stuff. The brick movement updates, and a somewhat stable 60 fps cap]---
    @Override
    public void run() {
        while(true) {
            frameCounter++;
            if (frameCounter >= 2) {
                if (brickY < screenHeight - brickPixel) {
                    brickY++;
                }
                frameCounter = 0;
            }

            repaint();
            try { Thread.sleep(16); }
            catch (Exception e) {}

        }
    }

    //---[The texturing stuff]---
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (brickImg != null) {
            g2.drawImage(brickImg, brickX * winScale, (brickY - 6) * winScale, brickPixel * winScale, (brickPixel + 6) * winScale, null);
        }
    }

    //---[Background music!!!]---
    private void playBgMusic() {
        try {
            File musicFile = new File("resources/music/Bad Apple!! (PJSK collab).wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            Clip musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        }
        catch (Exception e) { System.out.println("Audio Error:" + e.getMessage()); }
    }





}
