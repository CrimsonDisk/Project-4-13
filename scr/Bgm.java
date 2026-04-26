//---[Background music class]---
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Bgm {
    private Clip musicClip;
    public void playAudio(String audioPath) {
        try {
            File musicFile = new File(audioPath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            Clip musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        }
        catch (Exception e) { System.out.println("Audio Error:" + e.getMessage()); }
    }

    public void stopMS() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }
}
