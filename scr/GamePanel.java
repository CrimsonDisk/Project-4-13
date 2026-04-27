import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable {
    private int winScale;
    private int cols = 13; 
    private int rows = 21; 
    private final int brickPixelHitBox = 13; 
    
    private int setSpeedBase = 1;
    private int setSpeedFastFalling = 8;
    private int movePixelByFrame = 2;

    private int[][] brickboard = new int[rows][cols];
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();

    private int screenWidth = 13 * cols;
    private int screenHeight = 13 * rows;

    private boolean isFastFalling = false;
    private java.util.Random rand = new java.util.Random();

    private int currentX, currentY; 
    private int[][] currentShape;    
    private int currentType;         
    private BufferedImage[] brickTexture = new BufferedImage[6];
    private Thread gameThread;

    
    private final int[][][] SHAPES = {
        {{0,1}, {1,1}, {2,1}, {3,1}}, 
        {{0,0}, {1,0}, {0,1}, {1,1}}, 
        {{1,0}, {0,1}, {1,1}, {2,1}}, 
        {{1,0}, {2,0}, {0,1}, {1,1}}, 
        {{0,0}, {1,0}, {1,1}, {2,1}}, 
        {{0,0}, {0,1}, {1,1}, {2,1}}, 
        {{2,0}, {0,1}, {1,1}, {2,1}}, 
    };

    public GamePanel(int winScale) {
        this.winScale = winScale;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        loadTexture();
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");
        spawnBrick();

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    if (canMove(currentX - brickPixelHitBox, currentY, currentShape)) currentX -= brickPixelHitBox;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    if (canMove(currentX + brickPixelHitBox, currentY, currentShape)) currentX += brickPixelHitBox;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    rotateShape();
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) { isFastFalling = true; }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) System.exit(0);
            }
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) isFastFalling = false;
            }
        });
    }

    
    private boolean canMove(int newX, int newY, int[][] shape) {
        for (int[] p : shape) {
            
            int targetX = (newX / brickPixelHitBox) + p[0];
            int targetY = (newY + brickPixelHitBox - 1) / brickPixelHitBox + p[1] - 1;
            
            
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
        currentX = brickPixelHitBox * 5; 
        currentY = 0;
        
        
        if (!canMove(currentX, currentY, currentShape)) {
            brickboard = new int[rows][cols]; 
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
        int frameCounter = 0;
        while(gameThread != null) {
            background.updateAnimation();
            frameCounter++;

            if (frameCounter >= movePixelByFrame) {
                int speed = isFastFalling ? setSpeedFastFalling : setSpeedBase;
                for (int i = 0; i < speed; i++) {
                    
                    if (canMove(currentX, currentY + 1, currentShape)) {
                        currentY++;
                    } else {
                        
                        lockBlockToGrid();
                        checkForFullRow();
                        spawnBrick();
                        break;
                    }
                }
                frameCounter = 0;
            }
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    
    private void lockBlockToGrid() {
        for (int[] p : currentShape) {
            int gridX = (currentX / brickPixelHitBox) + p[0];
            int gridY = (currentY / brickPixelHitBox) + p[1];
            
            if (gridY >= 0 && gridY < rows && gridX >= 0 && gridX < cols) {
                brickboard[gridY][gridX] = (currentType % 6) + 1;
            }
        }
    }

    private void checkForFullRow() {
        for (int r = rows - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < cols; c++) if (brickboard[r][c] == 0) full = false;
            if (full) {
                for (int row = r; row > 0; row--) brickboard[row] = brickboard[row-1].clone();
                brickboard[0] = new int[cols];
                r++; 
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        background.drawGrid(g2, rows, cols, brickPixelHitBox, winScale);

        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickboard[r][c] > 0) {
                    drawBrick(g2, c * brickPixelHitBox, r * brickPixelHitBox, brickboard[r][c] - 1);
                }
            }
        }

        
        for (int[] p : currentShape) {
            drawBrick(g2, currentX + (p[0] * brickPixelHitBox), currentY + (p[1] * brickPixelHitBox), currentType % 6);
        }
    }

    private void drawBrick(Graphics2D g2, int x, int y, int texIdx) {
        if (brickTexture[texIdx] != null) {
            
            g2.drawImage(brickTexture[texIdx], x * winScale, (y - 6) * winScale, brickPixelHitBox * winScale, (brickPixelHitBox + 6) * winScale, null);
        }
    }
}