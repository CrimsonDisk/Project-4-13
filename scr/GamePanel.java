import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable {
    // ---[Thông số hệ thống]---
    private int winScale;
    private final int cols = 13; 
    private final int rows = 21; 
    private final int brickSize = 13;
    private final int UI_SIDEBAR_WIDTH = 100; 
    private final int UI_MARGIN = 10;

    private int score = 0;
    private boolean isGameOver = false;
    private int[][] brickboard = new int[rows][cols];
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();
    private int currentX, currentY, currentType, nextType;
    private int[][] currentShape, nextShape;
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
        int totalWidth = (cols * brickSize + UI_SIDEBAR_WIDTH + UI_MARGIN * 3) * winScale;
        int totalHeight = (rows * brickSize + UI_MARGIN * 2) * winScale;
        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
        this.setBackground(new Color(25, 25, 25));
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");
        nextType = rand.nextInt(SHAPES.length);
        nextShape = SHAPES[nextType];
        spawnBrick();

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. VẼ KHUNG CHƠI CHÍNH
        int boardX = UI_MARGIN * winScale;
        int boardY = UI_MARGIN * winScale;
        int boardW = cols * brickSize * winScale;
        int boardH = rows * brickSize * winScale;
        g2.setColor(Color.BLACK);
        g2.fillRect(boardX, boardY, boardW, boardH);
        
        g2.translate(boardX, boardY);
        background.drawGrid(g2, rows, cols, brickSize, winScale);
        
        // Vẽ gạch đã cố định
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) drawPerfectBrick(g2, c * brickSize, r * brickSize, brickboard[r][c] - 1);
            }
        }

        if (!isGameOver) {
            // ---[ VẼ BÓNG CỦA KHỐI (GHOST BLOCK) ]---
            int ghostY = currentY;
            // Tìm vị trí thấp nhất có thể rơi xuống
            while (canMove(currentX, ghostY + brickSize, currentShape)) {
                ghostY += brickSize;
            }
            
            // Thiết lập độ trong suốt cho bóng (30% độ đậm)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            for (int[] p : currentShape) {
                drawPerfectBrick(g2, currentX + (p[0] * brickSize), ghostY + (p[1] * brickSize), currentType % 6);
            }
            // Trả lại độ đậm 100% để vẽ các phần khác
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Vẽ khối gạch thật đang rơi
            for (int[] p : currentShape) {
                drawPerfectBrick(g2, currentX + (p[0] * brickSize), currentY + (p[1] * brickSize), currentType % 6);
            }
        }
        g2.translate(-boardX, -boardY);

        // 2. VẼ SIDEBAR (SCORE & NEXT)
        int sideX = boardX + boardW + UI_MARGIN * winScale;
        int sideW = UI_SIDEBAR_WIDTH * winScale;
        
        // VẼ SCORE
        int scoreY = UI_MARGIN * winScale;
        g2.setColor(Color.WHITE);
        g2.drawRect(sideX, scoreY, sideW, 50 * winScale);
        g2.setFont(new Font("Arial", Font.BOLD, 12 * winScale));
        drawCenteredString(g2, "SCORE", sideX, scoreY + 18 * winScale, sideW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16 * winScale));
        drawCenteredString(g2, String.format("%06d", score), sideX, scoreY + 40 * winScale, sideW);

        // VẼ NEXT (CĂN GIỮA)
        int nextBoxY = boardY + boardH - sideW;
        g2.setColor(Color.WHITE);
        g2.drawRect(sideX, nextBoxY, sideW, sideW);
        drawCenteredString(g2, "NEXT", sideX, nextBoxY + 20 * winScale, sideW);

        int minX = 4, maxX = 0, minY = 4, maxY = 0;
        for (int[] p : nextShape) {
            if (p[0] < minX) minX = p[0]; if (p[0] > maxX) maxX = p[0];
            if (p[1] < minY) minY = p[1]; if (p[1] > maxY) maxY = p[1];
        }
        int shapeW = (maxX - minX + 1) * brickSize * winScale;
        int shapeH = (maxY - minY + 1) * brickSize * winScale;
        int nX = sideX + (sideW - shapeW) / 2 - (minX * brickSize * winScale);
        int nY = nextBoxY + 25 * winScale + (sideW - 25 * winScale - shapeH) / 2 - (minY * brickSize * winScale);

        for (int[] p : nextShape) {
            if (brickTexture[nextType % 6] != null)
                g2.drawImage(brickTexture[nextType % 6], nX + p[0]*brickSize*winScale, nY + p[1]*brickSize*winScale, brickSize*winScale, brickSize*winScale, null);
        }

        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 24 * winScale));
            drawCenteredString(g2, "GAME OVER", 0, getHeight() / 2, getWidth());
        }
    }

    // ---[ HÀM VẼ GẠCH VÀ HÀM PHỤ TRỢ ]---
    private void drawPerfectBrick(Graphics2D g2, int x, int y, int texIdx) {
        if (brickTexture[texIdx] != null) {
            g2.drawImage(brickTexture[texIdx], x * winScale, y * winScale, brickSize * winScale, brickSize * winScale, null);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRect(x * winScale, y * winScale, brickSize * winScale, brickSize * winScale);
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y, int width) {
        FontMetrics m = g2.getFontMetrics();
        g2.drawString(text, x + (width - m.stringWidth(text)) / 2, y);
    }

    private void loadTexture() {
        String[] names = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan"};
        try { for (int i = 0; i < names.length; i++) brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + names[i] + " Brick.png")); }
        catch (Exception e) { System.out.println("Texture error"); }
    }

    private void spawnBrick() {
        currentType = nextType; currentShape = nextShape; currentX = brickSize * 5; currentY = 0;
        nextType = rand.nextInt(SHAPES.length); nextShape = SHAPES[nextType];
        if (!canMove(currentX, currentY, currentShape)) isGameOver = true;
    }

    private boolean canMove(int nX, int nY, int[][] s) {
        for (int[] p : s) {
            int tX = (nX / brickSize) + p[0], tY = (nY / brickSize) + p[1];
            if (tX < 0 || tX >= cols || tY >= rows) return false;
            if (tY >= 0 && brickboard[tY][tX] > 0) return false;
        }
        return true;
    }

    private void rotateShape() {
        int[][] r = new int[4][2];
        for (int i = 0; i < 4; i++) { r[i][0] = 2 - currentShape[i][1]; r[i][1] = currentShape[i][0]; }
        if (canMove(currentX, currentY, r)) currentShape = r;
    }

    private void lockToGrid() {
        for (int[] p : currentShape) {
            int gX = (currentX / brickSize) + p[0], gY = (currentY / brickSize) + p[1];
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

    private int fallDelay = 40, fastFallDelay = 5; private boolean isFastFalling = false;
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