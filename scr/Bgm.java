//---[Background music class]---
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Bgm {
    private Clip musicClip;

    public void playAudio(String audioPath) {
        try {
            File musicFile = new File(audioPath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            musicClip = AudioSystem.getClip(); 
            musicClip.open(audioStream);

            setVolume(-12.0f);

            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        }
        catch (Exception e) { System.out.println("Audio Error:" + e.getMessage()); }
    }

    public void setVolume(float volume) {
        try {
            if (musicClip != null && musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            }
        } catch (Exception e) { System.out.println("Volume Error: " + e.getMessage()); }
    }

    public void stopMS() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }
}