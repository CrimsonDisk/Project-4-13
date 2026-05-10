import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

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

    public Player selectPlayer() {
        String[] options = new String[players.size() + 1];
        for (int i = 0; i < players.size(); i++) { // Regular 0 to [Numbers of profiles - 1] for actual profiles
            options[i] = players.get(i).getName() + " - High Score: " + players.get(i).getScore();
        }
        options[players.size()] = "Add Player";

        int selection = JOptionPane.showOptionDialog(null, "Select your profile:", "Player List (Beta4.1)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selection == -1) System.exit(0);

        if (selection == players.size()) { // This choose the last option, which will be the create function, neat
            String newName = JOptionPane.showInputDialog("Enter name:");
            if (newName == null || newName.trim().isEmpty()) { newName = "Nameless Player"; }
            Player newPlayer = new Player(newName, 0);
            players.add(newPlayer);
            savePlayers();
            return newPlayer;
        }
        return players.get(selection);
    }

    // This is used to save the players, and is called after creating a new player, or when updating the score of an existing player
    public void savePlayers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(players);
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked") // So this just shuts the warning down, Java only knows it is an object, this tells it to this is the player array, I think

    // The player list loader, yaho
    private void loadPlayers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            players = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            players = (ArrayList<Player>) ois.readObject();
        } catch (Exception e) {
            players = new ArrayList<>();
        }
    }

    public ArrayList<Player> getPlayers() { return players; }
}