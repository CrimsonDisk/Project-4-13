import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    // --- [Main window, UI and scaling stuff] ---
    private Ui ui; // Reference to the UI class to update score and next piece preview
    private int winScale; // Scale, literally
    private int screenWidth = brickPixelHitBox * cols;
    private int screenHeight = brickPixelHitBox * rows;
    public static final int previewNextPiecePositionY = 170; // Y position for the next piece preview in the UI, default is 170

    // --- [Blocks and Grid Configuration] ---
    public static final int cols = 13; // Default is 13
    public static final int rows = 21; //Default is 21
    private int startCol = 5; // Starting column for the falling pieces, default is 5
    private int brickX = brickPixelHitBox * startCol; // X starting position of the falling piece
    private int brickY = 0; // Falling pieces start at the top
    private BufferedImage[] brickTexture = new BufferedImage[numberOfBaseBlocks + 1];

    // --- [Global Constants and variables] ---
    public static final int brickPixelHitBox = 13; // Size of the block itself
    public static final int bombSpawnRate = 35; // Rate at which bombs spawn
    private float previewPieceTransparency = 0.3f; // Transparency for the ghost/preview piece
    private int setSpeedBase = 1; // Base falling speed (in pixels per frame)
    private int setSpeedFastFalling = 6; // Falling speed when the down key is held (in pixels per frame, again)
    private int movePixelByFrame = 2; // Number of pixels the piece moves down every time the frame counter reaches the threshold, this is used to control the overall falling speed of the pieces, as it works together with the setSpeedBase and setSpeedFastFalling variables. Default is 2, which means the piece will move down by 2 pixels every time the frame counter reaches the threshold defined by setSpeedBase or setSpeedFastFalling.

    // --- [Core Gameplay Stuff] ---
    private int[][] brickboard = new int[rows][cols]; // The single most important thing, slots to put blocks in, wahoo. It's a 2D array, 0 is empty, >= 1 is occupied.
    private int[][] currentShape; // The shape that is currently falling
    private int[][] nextShape; // The shape that will fall after the current one, this is used for the "Next" preview in the UI, and also to determine the colour and bomb placement of the next piece before it becomes current.
    private int nextBrickIDColour = 1; // Colour ID for next piece, placeholder until it is randomized again and moved to current, default is 1 (purple)
    private int currentBrickIDColour = 1; // Colour ID for current piece, default is 1 (purple)
    public static final int numberOfBaseBlocks = 6; // Number of normal block types, this is used to determine the range for randomizing block colours, default is 6 (purple, red, orange, yellow, green, cyan)
    private int specialBlockIndex = -1; // Index for the special block (but there's only bomb sadly) in the current piece, -1 means no special block, default is -1
    private int nextSpecialBlockIndex = -1; // Same thing, just for preview, default is -1
    public static final int bombID = numberOfBaseBlocks + 1; // 1 is the ID for the bomb block, used to identify bombs on the board and in the pieces. Made it like this so I can add more easily

    // --- [Background music and Sounds] ---
    private Bgm musicPlayer = new Bgm();
    private Bg background = new Bg();
    private Sfx sfxPlayer = new Sfx();

    // --- [Miscellaneous] ---
    private boolean isFastFalling = false; // Fast fall check
    private java.util.Random rand = new java.util.Random(); // Random random!
    private boolean isGameOver = false; // Game over flag
    private int gameOverOption = 0; // 0 for retry, 1 for exit, default is 0
    private int frameCounter = 0; // Frame counter for controlling falling speed

    // --- [Main Constructor for the game, with key controls] ---
    public GamePanel(int winScale, Ui ui) {
        // Set up the main window and UI reference
        this.winScale = winScale;
        this.ui = ui;
        this.setPreferredSize(new Dimension(screenWidth * winScale, screenHeight * winScale));
        this.setBackground(Color.BLACK);

        // Loads texture for the entire game
        loadTexture();

        // Start the background music
        musicPlayer.playAudio("resources/music/Bad Apple!! (PJSK collab).wav");

        // Spawn the first shape to get things going, this also initializes the next shape and its colour and bomb placement for the preview
        spawnNewShape();

        // Start the game loop thread
        Thread gameThread = new Thread(this);
        gameThread.start();

        // Key! Controls! (This is a lot of code, but it's mostly just key controls and some game over menu stuff)
        this.setFocusable(true); // So the panel can receive key events
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (isGameOver) { // If the game is over, only allow up/down and enter for menu navigation and selection
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

                // Main game controls
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

    //---[The shape spawn thing]---
    private void spawnNewShape() {

        // Creates the next shape of bricks if it doesn't exist, and randomizes its colour and bomb placement
        if (nextShape == null) {
            nextShape = Shapes.getRandomShape();
            nextBrickIDColour = rand.nextInt(numberOfBaseBlocks) + 1; // Randomize colour for the next piece, +1 because 0 is empty
            nextSpecialBlockIndex = (rand.nextInt(100) < bombSpawnRate) ? rand.nextInt(8) : -1; // Randomize bomb placement for the next piece, if the random number is less than the bomb spawn rate, it will place a bomb in a random block of the piece, otherwise, there will be no bomb (-1)
        }
        // Movin' the created nextShape next to current (currentShape, literally)
        currentShape = nextShape;
        currentBrickIDColour = nextBrickIDColour;
        specialBlockIndex = nextSpecialBlockIndex;
        // We do the exact same thingy again, as the old "Next" shapw thingy becomes current, we make a new "Next", yippe.
        nextShape = Shapes.getRandomShape();
        nextBrickIDColour = rand.nextInt(numberOfBaseBlocks) + 1;
        nextSpecialBlockIndex = (rand.nextInt(100) < bombSpawnRate) ? rand.nextInt(4) : -1;

        // Update the next piece preview in the UI with the new next shape, colour, and bomb placement
        ui.setNextShapeData(nextShape, nextBrickIDColour, nextSpecialBlockIndex);

        //Bring the falling bricks back to its starting point, simple (but it took me a lot of time to realise that the falling brick, once it collides, make a clone of itself, and the falling brick returns to its starting point under a new appearance).
        brickY = 0;
        brickX = brickPixelHitBox * startCol;

        // Check if the new current shape spawns in a valid position, if not, game over, literally, again!
        if (!isValidPosition(brickX, brickY, currentShape)) {
            isGameOver = true;
            sfxPlayer.playSFX("resources/sfx/forklift-certified.wav");
        }
    }

    // --- [Reset game function, new board, new score, reset gameover flag, and spawn a new shape to start the game again] ---
    private void resetGame() {
        brickboard = new int[rows][cols];
        ui.resetScore();
        isGameOver = false;
        gameOverOption = 0;
        spawnNewShape();
    }

    // --- [Position check stuff] ---
    private boolean isValidPosition(int x, int y, int[][] shape) { // Yep, this flag is used a lot
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    // Calculate the target position, aka, where the block will be if it is placed at the current x and y with the current shape's configuration, this is used to check for collisions and boundaries
                    int targetX = (x / brickPixelHitBox) + c;
                    int targetY = (y / brickPixelHitBox) + r;

                    // Left and right wall boundaries
                    if (targetX < 0 || targetX >= cols || targetY >= rows) return false;
                    
                    // Collision with already placed blocks
                    if (targetY >= 0 && brickboard[targetY][targetX] > 0) return false;
                }
            }
        }
        return true;
    }

    // --- [The Rotation Function, fork this] ---
    private void rotate(boolean clockwise) {
        int[][] rotated = Shapes.rotate(currentShape, clockwise); // Get the rotated version of the current shape
        int[] kicks = {0, -brickPixelHitBox, brickPixelHitBox, -brickPixelHitBox * 2, brickPixelHitBox * 2}; // Kick offsets to try if the initial rotation position is invalid, this is a simple implementation of wall kicks, it tries to move the piece left and right by one and two blocks to see if it can fit, default is 5 attempts (0, -1, +1, -2, +2)
        for (int kick : kicks) {
            // Check if the rotated position with the current kick is valid, if it is, apply the rotation and the kick, then return
            if (isValidPosition(brickX + kick, brickY, rotated)) {
                brickX += kick;
                currentShape = rotated;
                return;
            }
        }
    }

    // --- [The bomb activation stuff] ---
    private void triggerBomb(int col) {
        // Offset thingy (x - 1, x, and x + 1) for blast radius
        for (int offSet = -1; offSet <= 1; offSet++) {
            int targetCols = col + offSet;

            if (targetCols >= 0 && targetCols < cols) { // Check to prevent out of bounds destructions (don't break my window!)
                for (int r = 0; r < rows; r++) {
                    if (brickboard[r][targetCols] != 0) {
                        if (brickboard[r][targetCols] == bombID) {
                            brickboard[r][targetCols] = 0; // Remove the current found bomb to prevent infinite loops
                            triggerBomb(targetCols); //I didn't know there is a concept called Recursion, by triggering the code itself again before the column clear, so it creates another check task after the first one! WHAT?
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

    //---[This is the popping mechanic I think, there's so much now I'm confused]
    private void checkForFullRow() {
        for (int currentRow = rows - 1; currentRow > 0; currentRow--) {
            boolean rowIsFull = true; // Assume the row is full until we find an empty slot
            for (int currentCol = 0; currentCol < cols; currentCol++) {
                if (brickboard[currentRow][currentCol] == 0) { // Gasps, it's empty!
                    rowIsFull = false;
                    break;
                }
            }
            if (rowIsFull) {
                // Look for bombs in the full row
                for (int currentCol = 0; currentCol < cols; currentCol++) {
                    if (brickboard[currentRow][currentCol] == bombID) {
                        // Found a bomb, yes Rico, kaboom.
                        triggerBomb(currentCol); 
                    }
                }

                // Standard row clear behavior (if no bomb was hit, or after bomb yay)
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

    // --- [Block Texture Loader] ---
    private void loadTexture() {
        String[] colourNames = {"Purple", "Red", "Orange", "Yellow", "Green", "Cyan", "Bomb"};
        try {
            brickTexture = new BufferedImage[colourNames.length]; // Dynamic sized array!
            for (int i = 0; i < colourNames.length; i++) {
                brickTexture[i] = ImageIO.read(new File("resources/textures/bricks/" + colourNames[i] + " Brick.png"));
            }
        } catch (Exception e) { System.out.println("Texture Error: " + e.getMessage()); }
    }

    //---[Collision check for the ghost piece, this is used to determine how far down the ghost piece should be drawn, it works similarly to the isValidPosition function, but it checks for collisions at a specific position rather than just validating a position for the current falling piece]---
    private boolean checkPossibleCollision(int x, int y, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int boardX = (x / brickPixelHitBox) + c;
                    int boardY = (y / brickPixelHitBox) + r;

                    // Check for bottom floor boundary
                    if (boardY >= rows) return true;

                    // Check for wall boundaries (why do I need this again?)
                    if (boardX < 0 || boardX >= cols) return true;
                    
                    // Check for collision with existing bricks on board
                    if (boardY >= 0 && brickboard[boardY][boardX] > 0) return true;
                }
            }
        }
        return false;
    }

    //---[The brick preview]---
    private int getLandingY() {
        int snappedGhostGridPositionY = (brickY / brickPixelHitBox) * brickPixelHitBox; // Snaps the ghost piece to the grid, this prevent weird animations (caused by the smooth falling)
        int ghostY = snappedGhostGridPositionY;
        // Move the ghost piece down until it collides, this will give us the correct Y position to draw the ghost piece, which COULD be the landing position of the current falling piece
        while (!checkPossibleCollision(brickX, ghostY + brickPixelHitBox, currentShape)) {
            ghostY += brickPixelHitBox;
        }
        return ghostY;
    }

    // --- [The main game loop, a lot of codes, but mainly controls the flow of the game (falling, saving array, row clear and all that fancy things)] ---
    @Override
    public void run() {
        while(true) {
            if (!isGameOver) {
                background.updateAnimation();
                frameCounter++;

                if (frameCounter >= movePixelByFrame) { // Time to move the piece down by one step (or more if fast falling)
                    int currentSpeed = (isFastFalling) ? setSpeedFastFalling : setSpeedBase;
                    
                    for (int i = 0; i < currentSpeed; i++) { // Move the piece down one pixel at a time, checking for collision at each step
                        if (brickY % brickPixelHitBox == 0) { // Only check for landing when the piece is aligned with the grid
                            if (!isValidPosition(brickX, brickY + brickPixelHitBox, currentShape)) {
                                int landedBlockCount = 0; // Used to determine which block in the current shape is special (bomb)

                                for (int r = currentShape.length - 1; r >= 0; r--) {
                                    for (int c = 0; c < currentShape[r].length; c++) {
                                        if (currentShape[r][c] == 1) {
                                            int gridY = (brickY / brickPixelHitBox) + r;
                                            int gridX = (brickX / brickPixelHitBox) + c;

                                            if (gridY >= 0 && gridY < rows && gridX >= 0 && gridX < cols) {
                                                if (landedBlockCount == specialBlockIndex) { // If this block is the special block (bomb), place a bomb ID in the brickboard instead of the normal colour ID
                                                    brickboard[gridY][gridX] = bombID;
                                                }
                                                else { brickboard[gridY][gridX] = currentBrickIDColour; }
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
            try { Thread.sleep(16); } catch (Exception e) {} // FPS roughly capped at 60
        }
    }

    // --- [Drawing Function] ---
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

            // Set transparency for faker pieces
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
            
            // Here will reset transparency to draw the REAL piece normally
            g2.setComposite(originalComposite);

            //---[The falling shapes part]---
            int fallingBlockCount = 0;
            for (int r = currentShape.length - 1; r >= 0; r--) {
                for (int c = 0; c < currentShape[r].length; c++) {
                    if (currentShape[r][c] == 1) {
                        int textureIndex = (fallingBlockCount == specialBlockIndex) ? 6 : (currentBrickIDColour - 1);

                        int drawX = (brickX + (c * brickPixelHitBox)) * winScale;
                        int drawY = (brickY + (r * brickPixelHitBox) - 6) * winScale;

                        g2.drawImage(brickTexture[textureIndex], 
                            drawX, drawY, 
                            brickPixelHitBox * winScale, 
                            (brickPixelHitBox + 6) * winScale, 
                            null);
                        fallingBlockCount++;
                    }
                }
            }
        }

        // ---[The brand new Game Over overlay]---
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