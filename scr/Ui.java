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
    private BufferedImage[] borderImgs = new BufferedImage[8];
    public static final int borderSize = 13;
    private int sidebarWideness = 10;
    private int sidebarHeightPixelBased = 927; // Will remain unused, cus buggy
    private Bg background = new Bg();

    private int scorePositionY = GamePanel.brickPixelHitBox * 3;
    private int scoreValuePositionY = GamePanel.brickPixelHitBox * 4;
    private int nextPositionY = GamePanel.brickPixelHitBox * 7;
    private int nextInLineShapeY = GamePanel.brickPixelHitBox * 8;

    // --- [Border Texture Loading] ---
    private void loadBorderTextures() {
        String[] borderTypes = {
            "topleft", "topright", "lowerleft", "lowerright", 
            "upperline", "lowerline", "lefthandline", "righthandline"
        };
        try {
            for (int i = 0; i < borderTypes.length; i++) {
                borderImgs[i] = ImageIO.read(new File("resources/textures/gui/border/" + borderTypes[i] + ".png"));
            }
        } catch (Exception e) {
            System.out.println("Border Load Error: " + e.getMessage());
        }
    }

    // --- [Next Piece Preview] ---
    public void setNextShapeData(int[][] shape, int colorID, int specialIndex) {
        this.nextShape = shape;
        this.nextColorID = colorID;
        this.nextSpecialIndex = specialIndex;
    }

    // --- [Constructors section] ---
    public Ui(int winScale) {
        this.winScale = winScale;
        loadBorderTextures();
        
        int gameWidth = GamePanel.cols * GamePanel.brickPixelHitBox * winScale; 
        int sidebarWidth = sidebarWideness * GamePanel.brickPixelHitBox * winScale;
        int totalHeight = (GamePanel.rows * GamePanel.brickPixelHitBox + (borderSize * 2)) * winScale;

        this.setPreferredSize(new Dimension(gameWidth + sidebarWidth + (borderSize * 2), totalHeight));
        this.setLayout(null); // Using null layout to PRECISELY position gamePanel inside the border
        this.setOpaque(false);

        gamePanel = new GamePanel(winScale, this);
        // Position gamePanel offset by the border size
        gamePanel.setBounds(borderSize * winScale, borderSize * winScale, gameWidth, GamePanel.rows * GamePanel.brickPixelHitBox * winScale);
        this.add(gamePanel);
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

    private Color getSimpleColor(int id) {
        switch(id) {
            case 1: return Color.MAGENTA;
            case 2: return Color.RED;
            case 3: return Color.ORANGE;
            case 4: return Color.YELLOW;
            case 5: return Color.GREEN;
            case 6: return Color.CYAN;
            default: return Color.WHITE;
        }
    }

    // --- [Rendering] ---
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        int bs = borderSize * winScale;
        int gw = gamePanel.getWidth();
        int gh = gamePanel.getHeight();

        // The sidebar background
        background.drawSidebarBG(g2, gw + bs, bs, getWidth() - (gw + bs), getHeight() - (bs * 2));
        
        //g2.setColor(new Color(30, 30, 30));
        //g2.fillRect(gw + bs, bs, getWidth() - (gw + bs), getHeight() - (bs * 2));

        if (borderImgs[0] != null) {
            // Corners
            g2.drawImage(borderImgs[0], 0, 0, bs, bs, null); // Top-left
            g2.drawImage(borderImgs[1], gw + bs, 0, bs, bs, null); // Top-right
            g2.drawImage(borderImgs[2], 0, gh + bs, bs, bs, null);
            g2.drawImage(borderImgs[3], gw + bs, gh + bs, bs,bs, null); // Bottom-right
            // Horizontal lines (top and bottom)
            for (int x = bs; x < gw + bs; x += bs) { // We don't start at 0, cus the corners will be there
                g2.drawImage(borderImgs[4], x, 0, bs, bs, null); // Top line
                g2.drawImage(borderImgs[5], x, gh + bs, bs, bs, null); // Bottom line
            }
            // Vertical lines (left and right)
            for (int y = bs; y < gh + bs; y += bs) {
                g2.drawImage(borderImgs[6], 0, y, bs, bs, null); // Left line
                g2.drawImage(borderImgs[7], gw + bs, y, bs, bs, null); // Right line
            }
        }

        //---[UI text styling stuff]---
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 10 * winScale));
        
        int textX = gw + (GamePanel.brickPixelHitBox * 3) * winScale; // Text position in sidebar, 4 blocks away from the game area

        g2.drawString("SCORE", textX, scorePositionY * winScale);
        g2.drawString(String.format("%06d", score), textX, scoreValuePositionY * winScale);

        g2.drawString("NEXT", textX, nextPositionY * winScale);

        // Draw the next piece preview, yosh
        if (nextShape != null) {
            int nextBlockCount = 0;

            for (int r = nextShape.length - 1; r >= 0; r--) {
                for (int c = 0; c < nextShape[r].length; c++) {
                    if (nextShape[r][c] == 1) {
                        int previewX = textX + (c * gamePanel.brickPixelHitBox * winScale);
                        int previewY = (nextInLineShapeY * winScale) + (r * gamePanel.brickPixelHitBox * winScale);

                        if (nextBlockCount == nextSpecialIndex) {
                            g2.setColor(Color.DARK_GRAY);
                        } else {
                            g2.setColor(getSimpleColor(nextColorID));
                        }

                        g2.fillRect(previewX, previewY, gamePanel.brickPixelHitBox * winScale - 1, gamePanel.brickPixelHitBox * winScale - 1);
                        nextBlockCount++;
                    }
                }
            }
        }
    }
}