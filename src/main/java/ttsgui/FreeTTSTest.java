import com.sun.speech.freetts.VoiceManager;

public class FreeTTSTest {
    public static void main(String[] args) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        // Get list of available voices
        VoiceManager voiceManager = VoiceManager.getInstance();
        com.sun.speech.freetts.Voice[] voices = voiceManager.getVoices();

        // Print out the available voices
        System.out.println("Available Voices:");
        for (com.sun.speech.freetts.Voice voice : voices) {
            System.out.println(voice.getName());
        }
    }
}
