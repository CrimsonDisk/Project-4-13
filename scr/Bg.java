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
    private int loadFrameAfterSecond = 500;

    public Bg() {
        try {
            for (int i = 0; i < frameNumber; i++) {
                gridFrames[i] = ImageIO.read(new File("resources/textures/gui/grid/grid-" + (i + 1) + ".png"));
            }
        }
        catch (Exception e) { System.out.println("Background Error: " + e.getMessage()); }
        lastUpdate = System.currentTimeMillis();
    }

    public void updateAnimation() {
        long ima = System.currentTimeMillis();
        if (ima - lastUpdate >= loadFrameAfterSecond) {
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
