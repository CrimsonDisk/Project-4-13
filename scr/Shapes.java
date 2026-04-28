import java.util.Random;

public class Shapes {
    //---[The shapes]---
    public static final int [][][] SHAPES = {
        { // O shape
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 0}
        },
        { // I shape
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}
        },
        { // L shape
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 1}
        },
        { // i shape
            {0, 1, 0},
            {0, 1, 0},
            {0, 0, 0}
        },
        { // J shape
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0}
        },
        { // dot shape
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
        },
        { // T shape
            {1, 1, 1},
            {0, 1, 0},
            {0, 1, 0}
        },        
        { // t shape
            {1, 1, 1},
            {0, 1, 0},
            {0, 0, 0}
        },        
        { // z shape
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0}
        },
        { // z' shape
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
        },
        { // Z shape
            {1, 1, 0},
            {0, 1, 0},
            {0, 1, 1}
        },
        { // Z' shape
            {0, 1, 1},
            {0, 1, 0},
            {1, 1, 0}
        },
    };

    public static int[][] getRandomShape() {
        return SHAPES[new Random().nextInt(SHAPES.length)];
    }

    public static int[][] rotate(int[][] matrix, boolean clockwise) {
        int n = 3;
        int[][] result = new int[n][n];

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (clockwise) {
                    result[col][n - 1 - row] = matrix[row][col];
                } else {
                    result[n - 1 - col][row] = matrix[row][col];
                }
            }
        }
        return result;
    }
}