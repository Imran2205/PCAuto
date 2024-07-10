package ttsgui;

import simplerobotinterfacelib.TTSListener;

//180 normal for amit bhai.
public class TTSGui implements TTSListener{
    TTS tts;
    GUI gui;
    public TTSGui() {
        tts = new TTS(this);
        gui=new GUI(this);
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
    }
    public static void main(String[] args) {
        new TTSGui();
    }
    
    public void onText(String txt) {
        tts.voice.setPitch(gui.getPitch());
        tts.speakText(txt);
    }

    @Override
    public void onTTSCompleted() {
        System.out.println("Speak Completed");
    }
    
}
