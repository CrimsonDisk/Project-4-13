import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    //---[Main window and scaling stuff]---
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
    private boolean historyShown = false; // BIẾN MỚI: Để kiểm soát việc hiện bảng lịch sử
    private int gameOverOption = 0;

    private int brickX = brickPixelHitBox * startCol;
    private int brickY = 0;
    private int frameCounter = 0;
    private BufferedImage[] brickTexture = new BufferedImage[numberOfBricks + 1];

    public GamePanel(int winScale, Ui ui) {
        this.winScale = winScale;
        this.ui = ui;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");

        spawnNewShape();

        Thread gameThread = new Thread(this);
        gameThread.start();

        this.setFocusable(true);
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                        gameOverOption = (gameOverOption == 0) ? 1 : 0;
                        sfxPlayer.playSFX("resources/sfx/MenuBleep.wav");
                        repaint();
                    }
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        sfxPlayer.playSFX("resources/sfx/MenuAccept.wav");
                        if (gameOverOption == 0) { resetGame(); }
                        else { System.exit(0); }
                    }
                    return;
                }

                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    if (isValidPosition(brickX - brickPixelHitBox, brickY, currentShape)) brickX -= brickPixelHitBox;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    if (isValidPosition(brickX + brickPixelHitBox, brickY, currentShape)) brickX += brickPixelHitBox;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_Q) {
                    rotate(false);
                    sfxPlayer.playSFX("resources/sfx/Land.wav");
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_E) {
                    rotate(true);
                    sfxPlayer.playSFX("resources/sfx/Land.wav");
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) { System.exit(0); }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) { isFastFalling = true; }
            }
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) { isFastFalling = false; }
            }
        });
    }

    private void spawnNewShape() {
        if (nextShape == null) {
            nextShape = Shapes.getRandomShape();
            nextBrickIDColour = rand.nextInt(numberOfBricks) + 1;
            nextSpecialBlockIndex = (rand.nextInt(100) < verticBrickPercent) ? rand.nextInt(4) : -1;
        }
        currentShape = nextShape;
        currentBrickIDColour = nextBrickIDColour;
        specialBlockIndex = nextSpecialBlockIndex;
        
        nextShape = Shapes.getRandomShape();
        nextBrickIDColour = rand.nextInt(numberOfBricks) + 1;
        nextSpecialBlockIndex = (rand.nextInt(100) < verticBrickPercent) ? rand.nextInt(4) : -1;

        ui.setNextShapeData(nextShape, nextBrickIDColour, nextSpecialBlockIndex);

        brickY = 0;
        brickX = brickPixelHitBox * startCol;

        if (!isValidPosition(brickX, brickY, currentShape)) {
            isGameOver = true;
            sfxPlayer.playSFX("resources/sfx/forklift-certified.wav");
            
            // KÍCH HOẠT MÀN HÌNH LỊCH SỬ TẠI ĐÂY
            if (!historyShown) {
                historyShown = true;
                SwingUtilities.invokeLater(() -> {
                    JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                    // Lấy điểm hiện tại từ đối tượng ui của bạn
                    GameOverHistory historyUI = new GameOverHistory(parent, ui.getScore());
                    historyUI.setVisible(true);
                });
            }
        }
    }

    private void resetGame() {
        brickboard = new int[rows][cols];
        ui.resetScore();
        isGameOver = false;
        historyShown = false; // RESET biến cờ khi chơi lại
        gameOverOption = 0;
        spawnNewShape();
    }

    private void rotate(boolean clockwise) {
        int[][] rotated = Shapes.rotate(currentShape, clockwise);
        int[] kicks = {0, -brickPixelHitBox, brickPixelHitBox, -brickPixelHitBox * 2, brickPixelHitBox * 2};
        for (int kick : kicks) {
            if (isValidPosition(brickX + kick, brickY, rotated)) {
                brickX += kick;
                currentShape = rotated;
                return;
            }
        }
    }

    private boolean isValidPosition(int x, int y, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int targetX = (x / brickPixelHitBox) + c;
                    int targetY = (y / brickPixelHitBox) + r;
                    if (targetX < 0 || targetX >= cols || targetY >= rows) return false;
                    if (targetY >= 0 && brickboard[targetY][targetX] > 0) return false;
                }
            }
        }
        return true;
    }

    private void triggerBomb(int col) {
        int bombID = 7;
        for (int offSet = -1; offSet <= 1; offSet++) {
            int targetCols = col + offSet; 
            if (targetCols >= 0 && targetCols < cols) {
                for (int r = 0; r < rows; r++) {
                    if (brickboard[r][targetCols] != 0) {
                        if (brickboard[r][targetCols] == bombID) {
                            brickboard[r][targetCols] = 0;
                            triggerBomb(targetCols);
                        } else {
                            ui.updateScore(1);
                            brickboard[r][targetCols] = 0;
                        }
                    }
                }
            }
        }
        sfxPlayer.playSFX("resources/sfx/SpiderBounce1.wav");
    }

    private void checkForFullRow() {
        int bombID = 7;
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
                    if (brickboard[currentRow][currentCol] == bombID) {
                        triggerBomb(currentCol); 
                    }
                }
                ui.updateScore(10); 
                for (int r = currentRow; r > 0; r--) {
                    for (int c = 0; c < cols; c++) {
                        brickboard[r][c] = brickboard[r - 1][c];
                    }
                }
                sfxPlayer.playSFX("resources/sfx/Destroy.wav");
                currentRow++; 
            }
        }
    }

    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan", "Bomb"};
        try {
            brickTexture = new BufferedImage[colourNames.length];
            for (int i = 0; i < colourNames.length; i++) {
                brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + colourNames[i] + " Brick.png"));
            }
        } catch (Exception e) { System.out.println("Texture Error: " + e.getMessage()); }
    }

    @Override
    public void run() {
        while(true) {
            if (!isGameOver) {
                background.updateAnimation();
                frameCounter++;
                if (frameCounter >= movePixelByFrame) {
                    int currentSpeed = (isFastFalling) ? setSpeedFastFalling : setSpeedBase;
                    for (int i = 0; i < currentSpeed; i++) {
                        if (brickY % brickPixelHitBox == 0) {
                            if (!isValidPosition(brickX, brickY + brickPixelHitBox, currentShape)) {
                                int landedBlockCount = 0;
                                for (int r = currentShape.length - 1; r >= 0; r--) {
                                    for (int c = 0; c < currentShape[r].length; c++) {
                                        if (currentShape[r][c] == 1) {
                                            int gridY = (brickY / brickPixelHitBox) + r;
                                            int gridX = (brickX / brickPixelHitBox) + c;
                                            if (gridY >= 0 && gridY < rows && gridX >= 0 && gridX < cols) {
                                                if (landedBlockCount == specialBlockIndex) brickboard[gridY][gridX] = numberOfBricks + 1;
                                                else brickboard[gridY][gridX] = currentBrickIDColour;
                                            }
                                            landedBlockCount++;
                                        }
                                    }
                                }
                                checkForFullRow();
                                sfxPlayer.playSFX("resources/sfx/MightyUnspin.wav");
                                spawnNewShape();
                                isFastFalling = false;
                                ui.repaint(); 
                                break;
                            }
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

    private boolean checkCollision(int x, int y, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int boardX = (x / brickPixelHitBox) + c;
                    int boardY = (y / brickPixelHitBox) + r;
                    if (boardY >= rows) return true;
                    if (boardX < 0 || boardX >= cols) return true;
                    if (boardY >= 0 && brickboard[boardY][boardX] > 0) return true;
                }
            }
        }
        return false;
    }

    private int getLandingY() {
        int snappedGhostGridPositionY = (brickY / brickPixelHitBox) * brickPixelHitBox;
        int ghostY = snappedGhostGridPositionY;
        while (!checkCollision(brickX, ghostY + brickPixelHitBox, currentShape)) {
            ghostY += brickPixelHitBox;
        }
        return ghostY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        background.drawGrid(g2, rows, cols, brickPixelHitBox, winScale);

        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) {
                    g2.drawImage(brickTexture[brickboard[r][c] - 1], 
                        c * brickPixelHitBox * winScale, 
                        (r * brickPixelHitBox - 6) * winScale, 
                        brickPixelHitBox * winScale, 
                        (brickPixelHitBox + 6) * winScale, 
                        null);
                }
            }
        }

        if (currentShape != null) {
            int landingY = getLandingY();
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewPieceTransparency));
            int fakerBlockCount = 0;
            for (int r = currentShape.length - 1; r >= 0; r--) {
                for (int c = 0; c < currentShape[r].length; c++) {  
                    if (currentShape[r][c] == 1) {
                        int textureIndex = (fakerBlockCount == specialBlockIndex) ? 6 : (currentBrickIDColour - 1);
                        int drawX = (brickX + (c * brickPixelHitBox)) * winScale;
                        int drawY = (landingY + (r * brickPixelHitBox) - 6) * winScale;
                        g2.drawImage(brickTexture[textureIndex], drawX, drawY, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
                        fakerBlockCount++;
                    }
                }
            }
            g2.setComposite(originalComposite);
            int fallingBlockCount = 0;
            for (int r = currentShape.length - 1; r >= 0; r--) {
                for (int c = 0; c < currentShape[r].length; c++) {
                    if (currentShape[r][c] == 1) {
                        int textureIndex = (fallingBlockCount == specialBlockIndex) ? 6 : (currentBrickIDColour - 1);
                        int drawX = (brickX + (c * brickPixelHitBox)) * winScale;
                        int drawY = (brickY + (r * brickPixelHitBox) - 6) * winScale;
                        g2.drawImage(brickTexture[textureIndex], drawX, drawY, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
                        fallingBlockCount++;
                    }
                }
            }
        }

        if (isGameOver) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            int rectW = 100 * winScale;
            int rectH = 60 * winScale;
            int rectX = (getWidth() - rectW) / 2;
            int rectY = (getHeight() - rectH) / 2;

            g2.setColor(new Color(40, 40, 40));
            g2.fillRect(rectX, rectY, rectW, rectH);
            g2.setColor(Color.WHITE);
            g2.drawRect(rectX, rectY, rectW, rectH);

            g2.setFont(new Font("Arial", Font.BOLD, 12 * winScale));
            g2.drawString("GAME OVER", rectX + 15 * winScale, rectY + 20 * winScale);

            g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
            g2.setColor(gameOverOption == 0 ? Color.RED : Color.WHITE);
            g2.drawString("RETRY", rectX + 30 * winScale, rectY + 40 * winScale);
            g2.setColor(gameOverOption == 1 ? Color.RED : Color.WHITE);
            g2.drawString("EXIT", rectX + 30 * winScale, rectY + 52 * winScale);
        }
    }
}