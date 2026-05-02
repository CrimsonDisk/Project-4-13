import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    //---[]---
    private Ui ui; 
    private int winScale;
    public static final int cols = 13; 
    public static final int rows = 21; 
    private int startCol = 5; 
    public static final int brickPixelHitBox = 13;
    public static final int verticBrickPercent = 30;
    private float previewPieceTransparency = 0.3f;
    public static final int previewNextPiecePositionY = 170;
    
    private int setSpeedBase = 1;
    private int setSpeedFastFalling = 6;
    private int movePixelByFrame = 2;

    private int[][] brickboard = new int[rows][cols];
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();
    private Sfx sfxPlayer = new Sfx();

    private int screenWidth = brickPixelHitBox * cols;
    private int screenHeight = brickPixelHitBox * rows;

    private int[][] currentShape;
    private int[][] nextShape;
    private int nextBrickIDColour = 1;
    private int nextSpecialBlockIndex = -1;
    public static final int numberOfBricks = 6;
    private int specialBlockIndex = -1;

    private boolean isFastFalling = false;
    private int currentBrickIDColour = 1;
    private java.util.Random rand = new java.util.Random();

    private boolean isGameOver = false;
    private boolean historyShown = false; 
    private int gameOverOption = 0;

    private int brickX = brickPixelHitBox * startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage[] brickTexture = new BufferedImage[numberOfBricks + 2];
    
    private BufferedImage bgImage; 
    private BufferedImage[] borderImgs = new BufferedImage[8];
    private final int borderSize = 16;

    public GamePanel(int winScale, Ui ui) {
        this.winScale = winScale;
        this.ui = ui;

        int totalWidth = (screenWidth + (borderSize * 2)) * winScale;
        int totalHeight = (screenHeight + (borderSize * 2)) * winScale;
        
        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
        this.setBackground(Color.BLACK);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");
        spawnNewShape();

        Thread gameThread = new Thread(this);
        gameThread.start();

        this.setFocusable(true);
        setupKeyListeners();
    }

    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan", "Bomb"};
        try {
            for (int i = 0; i < colourNames.length; i++) {
                brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + colourNames[i] + " Brick.png"));
            }
            bgImage = ImageIO.read(new File("resources/textures/backgrounds/bg_game.png"));

            borderImgs[0] = ImageIO.read(new File("resources/textures/gui/border/topleft.png"));
            borderImgs[1] = ImageIO.read(new File("resources/textures/gui/border/topright.png"));
            borderImgs[2] = ImageIO.read(new File("resources/textures/gui/border/lowerleft.png"));
            borderImgs[3] = ImageIO.read(new File("resources/textures/gui/border/lowerright.png"));
            borderImgs[4] = ImageIO.read(new File("resources/textures/gui/border/upperline.png"));
            borderImgs[5] = ImageIO.read(new File("resources/textures/gui/border/lowerline.png"));
            borderImgs[6] = ImageIO.read(new File("resources/textures/gui/border/lefthandline.png"));
            borderImgs[7] = ImageIO.read(new File("resources/textures/gui/border/righthandline.png"));
        } catch (Exception e) { System.out.println("Texture Error: " + e.getMessage()); }
    }

    private void drawBorder(Graphics2D g2) {
        if (borderImgs[0] == null) return;
        int s = borderSize * winScale;
        int w = getWidth();
        int h = getHeight();

        g2.drawImage(borderImgs[4], s, 0, w - (2 * s), s, null);      // Top
        g2.drawImage(borderImgs[5], s, h - s, w - (2 * s), s, null);  // Bottom
        g2.drawImage(borderImgs[6], 0, s, s, h - (2 * s), null);      // Left
        g2.drawImage(borderImgs[7], w - s, s, s, h - (2 * s), null);  // Right

        g2.drawImage(borderImgs[0], 0, 0, s, s, null);
        g2.drawImage(borderImgs[1], w - s, 0, s, s, null);
        g2.drawImage(borderImgs[2], 0, h - s, s, s, null);
        g2.drawImage(borderImgs[3], w - s, h - s, s, s, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int offset = borderSize * winScale;

        if (bgImage != null) {
            g2.drawImage(bgImage, offset, offset, screenWidth * winScale, screenHeight * winScale, null);
        }

        g2.translate(offset, offset);
        background.drawGrid(g2, rows, cols, brickPixelHitBox, winScale);

        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) {
                    g2.drawImage(brickTexture[brickboard[r][c] - 1], 
                        c * brickPixelHitBox * winScale, (r * brickPixelHitBox - 6) * winScale, 
                        brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
                }
            }
        }

        if (currentShape != null) {
            int landingY = getLandingY();
            Composite originalComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewPieceTransparency));
            drawPiece(g2, currentShape, brickX, landingY);
            g2.setComposite(originalComp);
            drawPiece(g2, currentShape, brickX, brickY);
        }

        g2.translate(-offset, -offset);
        drawBorder(g2);

        if (isGameOver) {
            drawGameOverOverlay(g2);
        }
    }

    private void drawPiece(Graphics2D g2, int[][] shape, int x, int y) {
        int blockCount = 0;
        for (int r = shape.length - 1; r >= 0; r--) {
            for (int c = 0; c < shape[r].length; c++) {  
                if (shape[r][c] == 1) {
                    int textureIndex = (blockCount == specialBlockIndex) ? 6 : (currentBrickIDColour - 1);
                    g2.drawImage(brickTexture[textureIndex], (x + (c * brickPixelHitBox)) * winScale, (y + (r * brickPixelHitBox) - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
                    blockCount++;
                }
            }
        }
    }

    private void spawnNewShape() {
        if (nextShape == null) {
            nextShape = Shapes.getRandomShape();
            nextBrickIDColour = rand.nextInt(numberOfBricks) + 1;
            nextSpecialBlockIndex = (rand.nextInt(100) < verticBrickPercent) ? rand.nextInt(4) : -1;
        }
        currentShape = nextShape; currentBrickIDColour = nextBrickIDColour; specialBlockIndex = nextSpecialBlockIndex;
        nextShape = Shapes.getRandomShape(); nextBrickIDColour = rand.nextInt(numberOfBricks) + 1;
        nextSpecialBlockIndex = (rand.nextInt(100) < verticBrickPercent) ? rand.nextInt(4) : -1;
        ui.setNextShapeData(nextShape, nextBrickIDColour, nextSpecialBlockIndex);
        brickY = 0; brickX = brickPixelHitBox * startCol;
        if (!isValidPosition(brickX, brickY, currentShape)) {
            isGameOver = true; sfxPlayer.playSFX("resources/sfx/forklift-certified.wav");
            if (!historyShown) {
                historyShown = true;
                SwingUtilities.invokeLater(() -> {
                    JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                    new GameOverHistory(parent, ui.getScore()).setVisible(true);
                });
            }
        }
    }

    public void run() {
        while(true) {
            if (!isGameOver) {
                background.updateAnimation();
                frameCounter++;
                if (frameCounter >= movePixelByFrame) {
                    int speed = isFastFalling ? setSpeedFastFalling : setSpeedBase;
                    for (int i = 0; i < speed; i++) {
                        if (brickY % brickPixelHitBox == 0) {
                            if (!isValidPosition(brickX, brickY + brickPixelHitBox, currentShape)) { lockPiece(); break; }
                        }
                        brickY++;
                    }
                    frameCounter = 0;
                }
            }
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    private void lockPiece() {
        int count = 0;
        for (int r = currentShape.length - 1; r >= 0; r--) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] == 1) {
                    int gy = (brickY / brickPixelHitBox) + r, gx = (brickX / brickPixelHitBox) + c;
                    if (gy >= 0 && gy < rows && gx >= 0 && gx < cols) brickboard[gy][gx] = (count == specialBlockIndex) ? 7 : currentBrickIDColour;
                    count++;
                }
            }
        }
        checkForFullRow(); sfxPlayer.playSFX("resources/sfx/MightyUnspin.wav"); spawnNewShape(); isFastFalling = false; ui.repaint();
    }

    private boolean isValidPosition(int x, int y, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int tx = (x / brickPixelHitBox) + c, ty = (y / brickPixelHitBox) + r;
                    if (tx < 0 || tx >= cols || ty >= rows) return false;
                    if (ty >= 0 && brickboard[ty][tx] > 0) return false;
                }
            }
        }
        return true;
    }

    private void checkForFullRow() {
        for (int r = rows - 1; r > 0; r--) {
            boolean full = true;
            for (int c = 0; c < cols; c++) if (brickboard[r][c] == 0) { full = false; break; }
            if (full) {
                for (int c = 0; c < cols; c++) if (brickboard[r][c] == 7) triggerBomb(c);
                ui.updateScore(10);
                for (int row = r; row > 0; row--) System.arraycopy(brickboard[row - 1], 0, brickboard[row], 0, cols);
                sfxPlayer.playSFX("resources/sfx/Destroy.wav"); r++;
            }
        }
    }

    private void triggerBomb(int col) {
        for (int off = -1; off <= 1; off++) {
            int tc = col + off;
            if (tc >= 0 && tc < cols) {
                for (int r = 0; r < rows; r++) {
                    if (brickboard[r][tc] != 0) {
                        if (brickboard[r][tc] == 7) { brickboard[r][tc] = 0; triggerBomb(tc); }
                        else { ui.updateScore(1); brickboard[r][tc] = 0; }
                    }
                }
            }
        }
    }

    private int getLandingY() {
        int gy = (brickY / brickPixelHitBox) * brickPixelHitBox;
        while (!checkCollision(brickX, gy + brickPixelHitBox, currentShape)) gy += brickPixelHitBox;
        return gy;
    }

    private boolean checkCollision(int x, int y, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int bx = (x / brickPixelHitBox) + c, by = (y / brickPixelHitBox) + r;
                    if (by >= rows || bx < 0 || bx >= cols) return true;
                    if (by >= 0 && brickboard[by][bx] > 0) return true;
                }
            }
        }
        return false;
    }

    private void setupKeyListeners() {
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == 38 || e.getKeyCode() == 40) { gameOverOption = 1 - gameOverOption; repaint(); }
                    if (e.getKeyCode() == 10) { if (gameOverOption == 0) resetGame(); else System.exit(0); }
                    return;
                }
                int k = e.getKeyCode();
                if (k == java.awt.event.KeyEvent.VK_ESCAPE) System.exit(0);
                if (k == 37 && isValidPosition(brickX - brickPixelHitBox, brickY, currentShape)) brickX -= brickPixelHitBox;
                if (k == 39 && isValidPosition(brickX + brickPixelHitBox, brickY, currentShape)) brickX += brickPixelHitBox;
                if (k == 81) rotate(false); if (k == 69) rotate(true);
                if (k == 40) isFastFalling = true;
            }
            public void keyReleased(java.awt.event.KeyEvent e) { if (e.getKeyCode() == 40) isFastFalling = false; }
        });
    }

    private void rotate(boolean cw) {
        int[][] rot = Shapes.rotate(currentShape, cw);
        for (int k : new int[]{0, -13, 13, -26, 26}) {
            if (isValidPosition(brickX + k, brickY, rot)) { brickX += k; currentShape = rot; break; }
        }
    }

    private void resetGame() { brickboard = new int[rows][cols]; ui.resetScore(); isGameOver = false; historyShown = false; spawnNewShape(); }

    private void drawGameOverOverlay(Graphics2D g2) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2.setColor(Color.BLACK); g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        int rw = 100 * winScale, rh = 60 * winScale;
        int rx = (getWidth() - rw)/2, ry = (getHeight() - rh)/2;
        g2.setColor(new Color(40, 40, 40)); g2.fillRect(rx, ry, rw, rh);
        g2.setColor(Color.WHITE); g2.drawRect(rx, ry, rw, rh);
        g2.setFont(new Font("Arial", Font.BOLD, 12 * winScale));
        g2.drawString("GAME OVER", rx + 15 * winScale, ry + 20 * winScale);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        g2.setColor(gameOverOption == 0 ? Color.RED : Color.WHITE);
        g2.drawString("RETRY", rx + 30 * winScale, ry + 40 * winScale);
        g2.setColor(gameOverOption == 1 ? Color.RED : Color.WHITE);
        g2.drawString("EXIT", rx + 30 * winScale, ry + 52 * winScale);
    }
}