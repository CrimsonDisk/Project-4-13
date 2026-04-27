//---[Sound effect class]---

import javax.sound.sampled.*;
import java.io.File;

public class Sfx {
    public void playSFX(String path) {
        try {
            File sfxFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(sfxFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) { clip.close(); }
            });
        }
        catch (Exception e) { System.out.println("SFX Error: " + e.getMessage());}
    }
}
