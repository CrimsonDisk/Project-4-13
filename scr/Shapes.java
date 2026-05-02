import java.util.Random;

public class Shapes {
    //---[The shapes]---
    public static final int [][][] SHAPES = {
        { // o shape - i0
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 0}
        },
        { // I shape - i1
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}
        },
        { // i shape - i2
            {0, 1, 0},
            {0, 1, 0},
            {0, 0, 0}
        },
        { // dot shape - i3
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
        },
        { // t shape - i4
            {1, 1, 1},
            {0, 1, 0},
            {0, 0, 0}
        },
        { // u shape - i5
            {1, 0, 1},
            {1, 1, 1},
            {0, 0, 0}
        },
        { // z shape - i6 --[Less frequent shapes]--
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0}
        },
        { // z' shape - i7
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
        },
        { // Door shape - i8
            {1, 1, 0},
            {1, 1, 0},
            {1, 1, 0}
        },
        { // L shape - i9 --[Starting here is hard, I think]--
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 1}
        },
        { // J shape - i10
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0}
        },
        { // T shape - i11
            {1, 1, 1},
            {0, 1, 0},
            {0, 1, 0}
        },
        { // Z shape - i12
            {1, 1, 0},
            {0, 1, 0},
            {0, 1, 1}
        },
        { // Z' shape - i13
            {0, 1, 1},
            {0, 1, 0},
            {1, 1, 0}
        },
        { // 3x3 shape - i14
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        },
        { // Cross shape - i15
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}
        },
        { // U shape - i16
            {1, 0, 1},
            {1, 0, 1},
            {1, 1, 1}
        },
        { // A shape - i17
            {1, 1, 1},
            {1, 1, 1},
            {1, 0, 1}
        },
    };

    public static int[][] getRandomShape() {
        Random rand = new Random();
        int roll = rand.nextInt(100);

        // 70% chance for indices 0-5
        if (roll < 70) { 
            return SHAPES[rand.nextInt(6)];
        } 
        // 20% change for indices 6-9
        else if (roll < 90) {
            return SHAPES[6 + rand.nextInt(3)];
        }
        // 10% chance for indices 9-17
        else {
            return SHAPES[9 + rand.nextInt(8)];
        }
    }

    public static int[][] rotate(int[][] matrix, boolean clockwise) {
        int n = matrix.length;
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