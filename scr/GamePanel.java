import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable {
    // ---[Cài đặt thông số]---
    private int winScale;
    private final int cols = 13; 
    private final int rows = 21; 
    private final int brickSize = 13;

    // ---[Thông số Giao diện]---
    private final int UI_SIDEBAR_WIDTH = 80; // Độ rộng phần bên phải
    private final int UI_MARGIN = 10;        // Khoảng cách lề

    private int score = 0;
    private boolean isGameOver = false;

    private int[][] brickboard = new int[rows][cols];
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();

    private int currentX, currentY; 
    private int[][] currentShape;    
    private int currentType;         

    // ---[Khối tiếp theo (Next Block)]---
    private int nextType;
    private int[][] nextShape;

    private BufferedImage[] brickTexture = new BufferedImage[6];
    private Thread gameThread;
    private java.util.Random rand = new java.util.Random();

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
        
        // Tính toán chiều rộng tổng: (Lưới game + Lề + Phần bên phải)
        int totalWidth = (cols * brickSize + UI_SIDEBAR_WIDTH + UI_MARGIN * 3) * winScale;
        int totalHeight = (rows * brickSize + UI_MARGIN * 2) * winScale;

        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
        this.setBackground(new Color(30, 30, 30)); // Màu nền xám đậm hiện đại
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");
        
        // Khởi tạo khối tiếp theo trước, sau đó mới spawn khối đầu tiên
        nextType = rand.nextInt(SHAPES.length);
        nextShape = SHAPES[nextType];
        spawnBrick();

        // [Logic điều khiển giữ nguyên]
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (isGameOver) { if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) restartGame(); return; }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) { if (canMove(currentX - brickSize, currentY, currentShape)) currentX -= brickSize; }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) { if (canMove(currentX + brickSize, currentY, currentShape)) currentX += brickSize; }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) rotateShape();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) isFastFalling = true;
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) System.exit(0);
            }
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) isFastFalling = false; }
        });
    }

    private void spawnBrick() {
        // Lấy khối từ "Next" chuyển thành "Current"
        currentType = nextType;
        currentShape = nextShape;
        currentX = brickSize * 5; 
        currentY = 0;

        // Tạo khối mới cho mục "Next"
        nextType = rand.nextInt(SHAPES.length);
        nextShape = SHAPES[nextType];

        if (!canMove(currentX, currentY, currentShape)) isGameOver = true;
    }

    // ---[Phần vẽ đồ họa GUI]---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. VẼ KHUNG CHƠI CHÍNH (Bên trái)
        int boardX = UI_MARGIN * winScale;
        int boardY = UI_MARGIN * winScale;
        g2.setColor(Color.BLACK);
        g2.fillRect(boardX, boardY, cols * brickSize * winScale, rows * brickSize * winScale);
        
        // Vẽ lưới và gạch bên trong khung chính
        g2.translate(boardX, boardY);
        background.drawGrid(g2, rows, cols, brickSize, winScale);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) drawPerfectBrick(g2, c * brickSize, r * brickSize, brickboard[r][c] - 1);
            }
        }
        if (!isGameOver) {
            for (int[] p : currentShape) drawPerfectBrick(g2, currentX + (p[0] * brickSize), currentY + (p[1] * brickSize), currentType % 6);
        }
        g2.translate(-boardX, -boardY); // Trả tọa độ về gốc

        // 2. VẼ KHUNG ĐIỂM SỐ (Góc phải trên)
        int sideX = (cols * brickSize + UI_MARGIN * 2) * winScale;
        int scoreBoxY = UI_MARGIN * winScale;
        int boxWidth = UI_SIDEBAR_WIDTH * winScale;
        int boxHeight = 40 * winScale;

        g2.setColor(Color.WHITE);
        g2.drawRect(sideX, scoreBoxY, boxWidth, boxHeight);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        g2.drawString("SCORE", sideX + 5 * winScale, scoreBoxY + 15 * winScale);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14 * winScale));
        g2.drawString(String.format("%06d", score), sideX + 10 * winScale, scoreBoxY + 32 * winScale);

        // 3. VẼ KHUNG NEXT BLOCK (Góc phải dưới)
        int nextBoxY = (rows * brickSize + UI_MARGIN - 60) * winScale;
        int nextBoxSize = UI_SIDEBAR_WIDTH * winScale;

        g2.setColor(Color.WHITE);
        g2.drawRect(sideX, nextBoxY, nextBoxSize, nextBoxSize);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        g2.drawString("NEXT", sideX + 5 * winScale, nextBoxY + 15 * winScale);

        // Vẽ khối tiếp theo vào trong ô vuông
        for (int[] p : nextShape) {
            int drawX = sideX + (p[0] * brickSize + 15) * winScale;
            int drawY = nextBoxY + (p[1] * brickSize + 25) * winScale;
            if (brickTexture[nextType % 6] != null) {
                g2.drawImage(brickTexture[nextType % 6], drawX, drawY, brickSize * winScale, brickSize * winScale, null);
            }
        }

        // 4. MÀN HÌNH GAME OVER (Nếu có)
        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 20 * winScale));
            g2.drawString("GAME OVER", getWidth()/4, getHeight()/2);
        }
    }

    // ---[Các hàm bổ trợ giữ nguyên logic của bạn]---
    private void drawPerfectBrick(Graphics2D g2, int x, int y, int texIdx) {
        if (brickTexture[texIdx] != null) {
            g2.drawImage(brickTexture[texIdx], x * winScale, y * winScale, brickSize * winScale, brickSize * winScale, null);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRect(x * winScale, y * winScale, brickSize * winScale, brickSize * winScale);
        }
    }

    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan"};
        try {
            for (int i = 0; i < colourNames.length; i++) {
                brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + colourNames[i] + " Brick.png"));
            }
        } catch (Exception e) { System.out.println("Lỗi tải Texture: " + e.getMessage()); }
    }

    private boolean canMove(int newX, int newY, int[][] shape) {
        for (int[] p : shape) {
            int tX = (newX / brickSize) + p[0];
            int tY = (newY / brickSize) + p[1];
            if (tX < 0 || tX >= cols || tY >= rows) return false;
            if (tY >= 0 && brickboard[tY][tX] > 0) return false;
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

    private void lockToGrid() {
        for (int[] p : currentShape) {
            int gX = (currentX / brickSize) + p[0];
            int gY = (currentY / brickSize) + p[1];
            if (gY >= 0 && gY < rows) brickboard[gY][gX] = (currentType % 6) + 1;
        }
    }

    private void checkForFullRow() {
        int lines = 0;
        for (int r = rows - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < cols; c++) if (brickboard[r][c] == 0) full = false;
            if (full) { lines++; for (int row = r; row > 0; row--) brickboard[row] = brickboard[row-1].clone(); brickboard[0] = new int[cols]; r++; }
        }
        if (lines == 1) score += 100; else if (lines == 2) score += 300; else if (lines == 3) score += 500; else if (lines >= 4) score += 800;
    }

    private void restartGame() { brickboard = new int[rows][cols]; score = 0; isGameOver = false; spawnBrick(); }

    public void startGameThread() { gameThread = new Thread(this); gameThread.start(); }

    private int fallDelay = 40; private int fastFallDelay = 5; private boolean isFastFalling = false;
    @Override
    public void run() {
        int count = 0;
        while(gameThread != null) {
            if (!isGameOver) {
                background.updateAnimation();
                int delay = isFastFalling ? fastFallDelay : fallDelay;
                if (count > delay) {
                    if (canMove(currentX, currentY + brickSize, currentShape)) currentY += brickSize;
                    else { lockToGrid(); checkForFullRow(); spawnBrick(); }
                    count = 0;
                }
                count++;
            }
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }
}