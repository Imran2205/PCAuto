/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ttsgui;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import simplerobotinterfacelib.TTSListener;

/**
 *
 * @author Sojib
 */
public class TTS {

    String voiceName = "kevin16";
    VoiceManager voiceManager;
    public Voice voice;
    TTSListener tl;
    public boolean isBusy = false;

    public TTS() {
        listAllVoices();
        System.out.println();
        System.out.println("Using voice: " + voiceName);

        voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);
        voice.setStyle("robotic");
        voice.allocate();
    }

    public TTS(TTSListener tl) {
        this.tl = tl;
        listAllVoices();
        System.out.println();
        System.out.println("Using voice: " + voiceName);

        voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);
//        voice.setStyle("robotic");
        voice.allocate();
    }

    private void listAllVoices() {
        System.out.println();
        System.out.println("All voices available:");
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (int i = 0; i < voices.length; i++) {
            System.out.println("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");
        }
    }

    public void speakText(String txt) {
        isBusy = true;
        voice.speak(txt);
        isBusy = false;
    }

    public boolean speakTextNb(String txt) {
        if (isBusy) {
            return false;
        }
        new NBSpeaker(txt).start();
        return true;
    }

    class NBSpeaker extends Thread {

        String txt = "";

        public NBSpeaker(String newTxt) {
            txt = newTxt;
        }

        public void run() {
            speakText(txt);
            txt = "";
            try {
                tl.onTTSCompleted();
            } catch (Exception ex) {
            }
        }
    }
}
