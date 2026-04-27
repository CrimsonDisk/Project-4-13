import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable {
    private int winScale;
    private final int cols = 13; 
    private final int rows = 21; 
    private final int brickSize = 13;

    private int score = 0;
    private boolean isGameOver = false; // Biến kiểm tra trạng thái kết thúc

    private int fallDelay = 40; 
    private int fastFallDelay = 5; 
    
    private int[][] brickboard = new int[rows][cols];
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();

    private int screenWidth = brickSize * cols;
    private int screenHeight = brickSize * rows;

    private boolean isFastFalling = false;
    private java.util.Random rand = new java.util.Random();

    private int currentX, currentY; 
    private int[][] currentShape;    
    private int currentType;         
    private BufferedImage[] brickTexture = new BufferedImage[6];
    private Thread gameThread;

    private final int[][][] SHAPES = {
        {{0,1}, {1,1}, {2,1}, {3,1}}, // I
        {{0,0}, {1,0}, {0,1}, {1,1}}, // O
        {{1,0}, {0,1}, {1,1}, {2,1}}, // T
        {{1,0}, {2,0}, {0,1}, {1,1}}, // S
        {{0,0}, {1,0}, {1,1}, {2,1}}, // Z
        {{0,0}, {0,1}, {1,1}, {2,1}}, // J
        {{2,0}, {0,1}, {1,1}, {2,1}}  // L
    };

    public GamePanel(int winScale) {
        this.winScale = winScale;
        this.setPreferredSize(new Dimension(screenWidth * winScale, (screenHeight + 30) * winScale));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");
        spawnBrick();

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) restartGame();
                    return;
                }
                
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    if (canMove(currentX - brickSize, currentY, currentShape)) currentX -= brickSize;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    if (canMove(currentX + brickSize, currentY, currentShape)) currentX += brickSize;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) rotateShape();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) isFastFalling = true;
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) System.exit(0);
            }
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) isFastFalling = false;
            }
        });
    }

    private void restartGame() {
        brickboard = new int[rows][cols];
        score = 0;
        isGameOver = false;
        spawnBrick();
    }

    private boolean canMove(int newX, int newY, int[][] shape) {
        for (int[] p : shape) {
            int targetX = (newX / brickSize) + p[0];
            int targetY = (newY / brickSize) + p[1];
            if (targetX < 0 || targetX >= cols || targetY >= rows) return false;
            if (targetY >= 0 && brickboard[targetY][targetX] > 0) return false;
        }
        return true;
    }

    private void rotateShape() {
        int[][] rotated = new int[4][2];
        for (int i = 0; i < 4; i++) {
            rotated[i][0] = 2 - currentShape[i][1];
            rotated[i][1] = currentShape[i][0];
        }
        if (canMove(currentX, currentY, rotated)) currentShape = rotated;
    }

    private void spawnBrick() {
        currentType = rand.nextInt(SHAPES.length);
        currentShape = SHAPES[currentType];
        currentX = brickSize * 5; 
        currentY = 0;
        
        // KIỂM TRA GAME OVER: Nếu không thể sinh khối mới -> Kết thúc
        if (!canMove(currentX, currentY, currentShape)) {
            isGameOver = true;
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan"};
        try {
            for (int i = 0; i < colourNames.length; i++) {
                brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + colourNames[i] + " Brick.png"));
            }
        } catch (Exception e) { System.out.println("Texture error: " + e.getMessage()); }
    }

    @Override
    public void run() {
        int count = 0;
        while(gameThread != null) {
            if (!isGameOver) { // Chỉ cập nhật logic nếu chưa Game Over
                background.updateAnimation();
                int currentDelay = isFastFalling ? fastFallDelay : fallDelay;
                if (count > currentDelay) {
                    if (canMove(currentX, currentY + brickSize, currentShape)) {
                        currentY += brickSize;
                    } else {
                        lockToGrid();
                        checkForFullRow();
                        spawnBrick();
                    }
                    count = 0;
                }
                count++;
            }
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    private void lockToGrid() {
        for (int[] p : currentShape) {
            int gridX = (currentX / brickSize) + p[0];
            int gridY = (currentY / brickSize) + p[1];
            if (gridY >= 0 && gridY < rows) brickboard[gridY][gridX] = (currentType % 6) + 1;
        }
    }

    private void checkForFullRow() {
        int linesCleared = 0;
        for (int r = rows - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < cols; c++) if (brickboard[r][c] == 0) full = false;
            if (full) {
                linesCleared++;
                for (int row = r; row > 0; row--) brickboard[row] = brickboard[row-1].clone();
                brickboard[0] = new int[cols];
                r++; 
            }
        }
        if (linesCleared == 1) score += 100;
        else if (linesCleared == 2) score += 300;
        else if (linesCleared == 3) score += 500;
        else if (linesCleared >= 4) score += 800;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        background.drawGrid(g2, rows, cols, brickSize, winScale);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) drawPerfectBrick(g2, c * brickSize, r * brickSize, brickboard[r][c] - 1);
            }
        }

        if (!isGameOver) {
            for (int[] p : currentShape) drawPerfectBrick(g2, currentX + (p[0] * brickSize), currentY + (p[1] * brickSize), currentType % 6);
        }

        // VẼ ĐIỂM SỐ
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        g2.drawString("SCORE: " + score, 10 * winScale, (screenHeight + 20) * winScale);

        // ---[HIỂN THỊ MÀN HÌNH GAME OVER]---
        if (isGameOver) {
            // Làm mờ màn hình bằng một lớp màu đen trong suốt
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Vẽ chữ GAME OVER
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 20 * winScale));
            String msg = "GAME OVER";
            
            // Căn giữa chữ
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g2.drawString(msg, x, y);

            // Hướng dẫn chơi lại
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 10 * winScale));
            String restartMsg = "Press ENTER to Restart";
            int rx = (getWidth() - g2.getFontMetrics().stringWidth(restartMsg)) / 2;
            g2.drawString(restartMsg, rx, y + (25 * winScale));
        }
    }

    private void drawPerfectBrick(Graphics2D g2, int x, int y, int texIdx) {
        if (brickTexture[texIdx] != null) {
            g2.drawImage(brickTexture[texIdx], x * winScale, y * winScale, brickSize * winScale, brickSize * winScale, null);
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawRect(x * winScale, y * winScale, brickSize * winScale, brickSize * winScale);
        }
    }
}