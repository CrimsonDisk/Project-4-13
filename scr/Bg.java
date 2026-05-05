//---[Background stuff]---
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Bg {
    private BufferedImage[] gridFrames = new BufferedImage[5];
    private int currentFrame = 0;
    private int frameDirection = 1;
    private long lastUpdate;

    private int frameNumber = 5;
    private int loadFrameAfterMilliSec = 400;

    private BufferedImage gameBG;
    private BufferedImage sidebarBG;

    public Bg() {
        try {
            // The animated grid part
            for (int i = 0; i < frameNumber; i++) {
                gridFrames[i] = ImageIO.read(new File("resources/textures/gui/grid/grid-" + (i + 1) + ".png"));
            }
            // The game and sidebar background
            gameBG = ImageIO.read(new File("resources/textures/gui/backgrounds/bg_game.png"));
            sidebarBG = ImageIO.read(new File("resources/textures/gui/backgrounds/bg_sidebar.png"));
        }
        catch (Exception e) { System.out.println("Background Error: " + e.getMessage()); }
        lastUpdate = System.currentTimeMillis();
    }

public void drawGameBG(Graphics2D g2, int panelWidth, int panelHeight) {
    if (gameBG != null) {
        // Draw starting at (0,0) and stretching to the full panel size
        g2.drawImage(gameBG, 0, 0, panelWidth, panelHeight, null);
    }
}

public void drawSidebarBG(Graphics2D g2, int xArea, int yArea, int areaWidth, int areaHeight) {
    if (sidebarBG != null) {
        // Draw starting at the sidebar's X/Y and stretching to the area size
        g2.drawImage(sidebarBG, xArea, yArea, areaWidth, areaHeight, null);
    }
}

    public void updateAnimation() {
        long ima = System.currentTimeMillis();
        if (ima - lastUpdate >= loadFrameAfterMilliSec) {
            currentFrame += frameDirection;
            if (currentFrame >= 4 || currentFrame <= 0) {
                frameDirection *= -1; //The order should go 1 to 5 then reverse back to 1, nice
            }
            lastUpdate = ima;
        }
    }

    public void drawGrid(Graphics2D g2, int rows, int cols, int brickSize, int scale) {
        if (gridFrames[currentFrame] == null)
            return;

        //Draws and update for each grid texture, nice
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g2.drawImage(gridFrames[currentFrame], (c * brickSize) * scale, (r * brickSize) * scale, brickSize * scale, brickSize * scale, null);

            }
        }
    }
}
