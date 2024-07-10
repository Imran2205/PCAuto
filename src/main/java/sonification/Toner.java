/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonification;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author FridayLab
 */
public class Toner extends Thread{

    //ducking enable. stop when new come.
    public static float SAMPLE_RATE = 8000f;

    public static boolean isplaying = false;

    public static void tone(int hz, int msecs, double vol) throws LineUnavailableException {
        isplaying = true;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i = 0; i < msecs * 8; i++) {
            if (!isplaying) {
                break;
            }
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
            sdl.write(buf, 0, 1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    public boolean isplay = false;

    public void stopPlay() {
        isplay = false;
    }

    int hz, msecs;
    double vol;

    public void setData(int hz, int msecs, double vol) {
        this.hz = hz;
        this.msecs = msecs;
        this.vol = vol;
    }

    public void run() {
        try {
            this.playTone(hz, msecs, vol);
        } catch (Exception ex) {
        }
    }

    public void start_tone(int hz, int msecs, double vol) throws LineUnavailableException {
        new Thread() {
            public void run() {
                try {
                    playTone(hz, msecs, vol);
                } catch (Exception ex) {
                }
            }
        }.start();
    }

    public void playTone(int hz, int msecs, double vol) throws LineUnavailableException {
        isplay = true;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i = 0; i < msecs * 8; i++) {
            if (!isplay) {
                break;
            }
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
            sdl.write(buf, 0, 1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

}
