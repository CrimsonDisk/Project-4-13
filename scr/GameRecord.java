public class GameRecord {
    private String name;
    private int score;

    public GameRecord(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() { return name; }
    public int getScore() { return score; }

    @Override
    public String toString() {
        return name + " - " + score;
    }
}