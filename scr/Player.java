import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L; // For compatibility during serialization, I suppose?
    private String name;
    private int score;

    public Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    @Override
    public String toString() {
        return name + " - High Score: " + score;
    }
}