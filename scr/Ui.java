import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Ui extends JPanel {
    private GamePanel gamePanel;
    private int score = 0;
    private int winScale;
    private int[][] nextShape;
    private int nextColorID;
    private int nextSpecialIndex;
    private BufferedImage sidebarBg;

    public void setNextShapeData(int[][] shape, int colorID, int specialIndex) {
        this.nextShape = shape;
        this.nextColorID = colorID;
        this.nextSpecialIndex = specialIndex;
    }

    public Ui(int winScale) {
        this.winScale = winScale;
        try {
            sidebarBg = ImageIO.read(new File("resources/textures/backgrounds/bg_sidebar.png"));
        } catch (Exception e) { System.out.println("Sidebar BG Error"); }

        int gameWidth = GamePanel.cols * GamePanel.brickPixelHitBox * winScale; 
        int borderPadding = 16 * 2 * winScale; // 16px viền trái + 16px viền phải
        int sidebarWidth = 8 * GamePanel.brickPixelHitBox * winScale;
        int totalHeight = (GamePanel.rows * GamePanel.brickPixelHitBox + (16 * 2)) * winScale;

        this.setPreferredSize(new Dimension(gameWidth + borderPadding + sidebarWidth, totalHeight));
        this.setLayout(new BorderLayout());
        gamePanel = new GamePanel(winScale, this);
        this.add(gamePanel, BorderLayout.WEST);
    }

    public void updateScore(int points) { this.score += points; }
    public void resetScore() { this.score = 0; }
    public int getScore() { return this.score; }

    private Color getSimpleColor(int id) {
        switch(id) {
            case 1: return Color.MAGENTA; case 2: return Color.RED;
            case 3: return Color.ORANGE; case 4: return Color.YELLOW;
            case 5: return Color.GREEN; case 6: return Color.CYAN;
            default: return Color.WHITE;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Bắt đầu sidebar ngay sau cái GamePanel (đã bao gồm viền gỗ)
        int sidebarX = gamePanel.getPreferredSize().width;
        int sidebarWidth = getWidth() - sidebarX;

        if (sidebarBg != null) {
            g2.drawImage(sidebarBg, sidebarX, 0, sidebarWidth, getHeight(), null);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        int textX = sidebarX + (15 * winScale);

        g2.drawString("SCORE", textX, 50 * winScale / 2);
        g2.drawString(String.format("%06d", score), textX, 80 * winScale / 2);
        g2.drawString("NEXT", textX, 150 * winScale / 2);

        if (nextShape != null) {
            int count = 0;
            for (int r = nextShape.length - 1; r >= 0; r--) {
                for (int c = 0; c < nextShape[r].length; c++) {
                    if (nextShape[r][c] == 1) {
                        int px = textX + (c * GamePanel.brickPixelHitBox * winScale);
                        int py = (GamePanel.previewNextPiecePositionY * winScale / 2) + (r * GamePanel.brickPixelHitBox * winScale);
                        g2.setColor(count == nextSpecialIndex ? Color.DARK_GRAY : getSimpleColor(nextColorID));
                        g2.fillRect(px, py, GamePanel.brickPixelHitBox * winScale - 1, GamePanel.brickPixelHitBox * winScale - 1);
                        count++;
                    }
                }
            }
        }
    }
}