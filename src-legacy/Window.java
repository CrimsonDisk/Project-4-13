import javax.swing.JFrame;


public class Window {

    public static final int WIDTH = 306, HEIGHT = 629;
    private JFrame window;
    private Board board;


    public Window(){
        window = new JFrame("Project 4-13");
        window.setSize(WIDTH, HEIGHT);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setLocationRelativeTo(null);

         board = new Board();
         window. add(board);
         window.addKeyListener(board);


        window.setVisible(true);

    }
    public static void main(String[] args) {
        new Window();
    }
}