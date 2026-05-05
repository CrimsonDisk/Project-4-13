import java.io.*;
import java.util.ArrayList;

public class PlayerManager {
    private static final String FILE_NAME = "players.dat";
    private ArrayList<Player> players;

    public PlayerManager() {
        loadPlayers();
        checkDefaultPlayer();
    }

    private void checkDefaultPlayer() {
        if (players.isEmpty()) {
            players.add(new Player("Player Ichi", 0));
            savePlayers();
        }
    }

    // --- [File Operations] ---
    
    // Saving and loading players using serialization
    public void savePlayers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(players);
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadPlayers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) { // If the file doesn't exist, start with an empty player list
            players = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            players = (ArrayList<Player>) ois.readObject();
        } catch (Exception e) { // If error, gives you an empty player list
            players = new ArrayList<>();
        }
    }
    
    // Getters
    public ArrayList<Player> getPlayers() { return players; }

}