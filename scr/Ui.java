import javax.swing.*;
import java.awt.*;

public class Ui extends JPanel {
    private GamePanel gamePanel;
    private int score = 0;
    private int[][] nextShape;
    private int winScale;

    public Ui(int winScale) {
        this.winScale = winScale;
        int gameWidth = gamePanel.cols * gamePanel.brickPixelHitBox * winScale; 
        int sidebarWidth = 8 * gamePanel.brickPixelHitBox * winScale;
        int totalHeight = gamePanel.rows * gamePanel.brickPixelHitBox * winScale;

        this.setPreferredSize(new Dimension(gameWidth + sidebarWidth, totalHeight));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.DARK_GRAY);

        gamePanel = new GamePanel(winScale, this);
        this.add(gamePanel, BorderLayout.WEST);
    }

    public void updateScore(int points) {
        this.score += points;
    }

    public void resetScore () {
        this.score = 0;
    }

    public void setNextShape(int[][] shape) {
        this.nextShape = shape;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // The sidebar background
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(gamePanel.getWidth(), 0, getWidth() - gamePanel.getWidth(), getHeight());

        //---[UI text styling stuff]---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        
        int textX = gamePanel.getWidth() + (20 * winScale / 2);

        g2.drawString("SCORE", textX, 50 * winScale / 2);
        g2.drawString(String.format("%06d", score), textX, 80 * winScale / 2);

        g2.drawString("NEXT", textX, 150 * winScale / 2);

        // Draw the next piece preview, yosh
        if (nextShape != null) {
            for (int r = 0; r < nextShape.length; r++) {
                for (int c = 0; c < nextShape[r].length; c++) {
                    if (nextShape[r][c] == 1) {
                        int previewX = textX + (c * gamePanel.brickPixelHitBox * winScale);
                        int previewY = (gamePanel.previewNextPiecePositionY * winScale / 2) + (r * gamePanel.brickPixelHitBox * winScale);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(previewX, previewY, gamePanel.brickPixelHitBox * winScale - 1, gamePanel.brickPixelHitBox * winScale - 1);
                    }
                }
            }
        }
    }
}