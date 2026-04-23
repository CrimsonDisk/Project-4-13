package src;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.Timer;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;

public class Board extends JPanel implements KeyListener {

    private BufferedImage blocks;
    private final int blockSize = 30;
    private final int boardWidth = 10, boardHeight = 20;
    private int[][] board = new int[boardHeight][boardWidth];

    private Shape[] shapes = new Shape[7];
    private Shape currentShape;
    private Timer timer;
    private final int FPS = 60;
    private final int delay = 1000 / FPS;
    private boolean gameOver = false;

    // --- BIẾN ĐIỂM SỐ ---
    private int score = 0;

    public Board() {
        try {
            blocks = ImageIO.read(Board.class.getResource("tiles.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
                repaint();
            }
        });

        timer.start();
        
        // Khởi tạo các loại khối
        shapes[0] = new Shape(blocks.getSubimage(0, 0, blockSize, blockSize), new int[][] { { 1, 1, 1, 1 } }, this, 1);
        shapes[1] = new Shape(blocks.getSubimage(blockSize, 0, blockSize, blockSize), new int[][] { { 1, 1, 0 }, { 0, 1, 1 } }, this, 2);
        shapes[2] = new Shape(blocks.getSubimage(blockSize * 2, 0, blockSize, blockSize), new int[][] { { 0, 1, 1 }, { 1, 1, 0 } }, this, 3);
        shapes[3] = new Shape(blocks.getSubimage(blockSize * 3, 0, blockSize, blockSize), new int[][] { { 1, 1, 1 }, { 0, 0, 1 } }, this, 4);
        shapes[4] = new Shape(blocks.getSubimage(blockSize * 4, 0, blockSize, blockSize), new int[][] { { 1, 1, 1 }, { 1, 0, 0 } }, this, 5);
        shapes[5] = new Shape(blocks.getSubimage(blockSize * 5, 0, blockSize, blockSize), new int[][] { { 1, 1, 1 }, { 0, 1, 0 } }, this, 6);
        shapes[6] = new Shape(blocks.getSubimage(blockSize * 6, 0, blockSize, blockSize), new int[][] { { 1, 1 }, { 1, 1 } }, this, 7);

        setNextShape();
    }

    public void update(){
        if(gameOver) {
            timer.stop();
            return;
        }
        currentShape.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. Vẽ các khối đã cố định trên bàn cờ
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if(board[row][col] != 0) {
                    g.drawImage(blocks.getSubimage((board[row][col] - 1)*blockSize, 0, blockSize, blockSize), 
                                col*blockSize, row*blockSize, null);
                }
            }
        }

        // 2. Vẽ khối đang rơi
        currentShape.render(g);

        // 3. Vẽ lưới bàn cờ
        g.setColor(Color.BLACK);
        for (int i = 0; i <= boardHeight; i++) {
            g.drawLine(0, i * blockSize, boardWidth * blockSize, i * blockSize);
        }
        for (int j = 0; j <= boardWidth; j++) {
            g.drawLine(j * blockSize, 0, j * blockSize, boardHeight * blockSize);
        }

        // --- 4. HIỂN THỊ ĐIỂM SỐ (DỄ NHÌN HƠN) ---
        g.setColor(Color.RED); // Chuyển sang màu đỏ cho nổi bật
        g.setFont(new Font("Verdana", Font.BOLD, 22));
        g.drawString("SCORE: " + score, 10, 30); 
        
        if (gameOver) {
            g.setColor(Color.WHITE);
            g.fillRect(50, 250, 200, 100);
            g.setColor(Color.RED);
            g.drawRect(50, 250, 200, 100);
            g.drawString("GAME OVER", 70, 310);
        }
    }

    public void setNextShape(){
        // BƯỚC QUAN TRỌNG: Kiểm tra xóa hàng trước khi tạo khối mới
        checkLine();

        int index = (int)(Math.random()*shapes.length);
        Shape newShape = new Shape(shapes[index].getBlock(), shapes[index].getCoords(), this, shapes[index].getColor());
        currentShape = newShape;

        // Kiểm tra thua cuộc
        for (int row = 0; row < currentShape.getCoords().length; row++) {
            for (int col = 0; col < currentShape.getCoords()[row].length; col++) {
                if(currentShape.getCoords()[row][col] != 0){
                    if(board[row][col + 3] != 0)
                        gameOver = true;
                }
            }
        }
    }

    // HÀM KIỂM TRA VÀ TÍNH ĐIỂM
    private void checkLine() {
        int linesCleared = 0;
        
        // Duyệt từ dưới lên trên
        for (int row = boardHeight - 1; row >= 0; row--) {
            int count = 0;
            for (int col = 0; col < boardWidth; col++) {
                if (board[row][col] != 0) {
                    count++;
                }
            }

            // Nếu hàng đã đầy (10 ô)
            if (count == boardWidth) {
                linesCleared++;
                // Đẩy mọi thứ phía trên xuống
                for (int r = row; r > 0; r--) {
                    for (int c = 0; c < boardWidth; c++) {
                        board[r][c] = board[r - 1][c];
                    }
                }
                // Xóa hàng trên cùng
                for (int c = 0; c < boardWidth; c++) {
                    board[0][c] = 0;
                }
                row++; // Kiểm tra lại hàng hiện tại sau khi dồn xuống
            }
        }

        // Cập nhật điểm dựa trên số hàng xóa được cùng lúc
        if (linesCleared > 0) {
            if (linesCleared == 1) score += 100;
            else if (linesCleared == 2) score += 300;
            else if (linesCleared == 3) score += 500;
            else if (linesCleared >= 4) score += 800;
            
            System.out.println("Lines Cleared: " + linesCleared + " | Current Score: " + score);
        }
    }

    public int getBlockSize() { return blockSize; }
    public int[][] getBoard(){ return board; }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if(gameOver) return;

        if(e.getKeyCode() == KeyEvent.VK_LEFT) currentShape.setDeltaX(-1);
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) currentShape.setDeltaX(1);
        if(e.getKeyCode() == KeyEvent.VK_DOWN) currentShape.speedDown();
        if(e.getKeyCode() == KeyEvent.VK_UP) currentShape.rotate();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN) currentShape.normalSpeed();
    }
}