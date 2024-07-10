import com.fazecast.jSerialComm.SerialPort;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import simplerobotinterfacelib.TTSListener;
import sonification.Toner;
import ttsgui.TTS;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.lang.*;


/**
 *
 * @author FridayLab
 */
public class BridgeKeyboard implements TTSListener {
    private int baud = 9600;
    private SerialPort wheel3d, keyboard;
    private InputStream wheel3d_i, keyboard_i;
    private OutputStream wheel3d_o, keyboard_o;
    private boolean isReady = false;

    private long prev_time = System.currentTimeMillis();
    private long curr_time = System.currentTimeMillis();

    private int config_xminfreq = 300;
    private int config_xmaxfreq = 800;
    private int config_yminfreq = 300;
    private int config_ymaxfreq = 800;
    private int config_tonevolume = 50;
    private int config_dingdongduration = 500;
    private boolean isMouseOnTild = true;
    private boolean isMouseOnCTRL = true;
    private boolean isMouse = true;
    private boolean isMouse_sim = false;
    private boolean enableDingDong = true;
    private boolean enableTTS = true;
    private boolean isTopLeft = true;


    TTS tts;
    Dimension screen;

    public BridgeKeyboard() {
        try {
            System.out.println("Connecting keyboard server");
//            keyboard = new SerialNR("COM17", baud, false); //will not wait for ready.
//            keyboard.enDebug();
//            SerialPort[] ports = SerialPort.getCommPorts();

            keyboard = SerialPort.getCommPort("cu.usbmodemHIDFG1");
            keyboard.setComPortParameters(9600, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            keyboard.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
//            ms.disDebug();
//            ms.enDebug();
            var hasOpened = keyboard.openPort();
            if(!hasOpened){
                System.out.println("Failed to open serial port.");
            }
            keyboard_i = keyboard.getInputStream();
            keyboard_o = keyboard.getOutputStream();
            System.out.println("connection initiated");

//            new read_keyboard().start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            System.out.println("Connecting wheel3d device");
//            wheel3d = new SerialNR("COM6", baud);
//            wheel3d.enDebug();

            wheel3d = keyboard;
            wheel3d_i = keyboard_i;
            wheel3d_o = keyboard_o;

            new read_wheel3d().start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        screen = Toolkit.getDefaultToolkit().getScreenSize();

        init_keyhook();
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        tts = new TTS(this);
        System.out.println(">>>>"+tts);
        tts.speakText("Ready");
//
    }

    boolean in_ribbon = false;
    boolean in_ribbon_nav = false;
    boolean on_hold = false;
    boolean is_selected = false;

    private void send_ser_data(OutputStream o_s, String msg){
        byte[] send_bytes = new byte[0];
        System.out.println(msg);
        try {
            send_bytes = msg.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            o_s.write(send_bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void on_wheels(int c1, int c2, int c3, int c5, int c6) {

        System.out.println("status: rib: "+in_ribbon+" nav: "+in_ribbon_nav+" hold: "+on_hold+" sel: "+is_selected);
        if (c6 == 1) {
            //esc
//            keyboard.send_new("p,177\n");   //esc
            send_ser_data(keyboard_o, "p,177\n");
            in_ribbon = false;
            in_ribbon_nav = false;
            return;
        } else if (c5 == 1) {
            if (in_ribbon) {
//                keyboard.send_new("p,176\n");   //ENTER
                send_ser_data(keyboard_o, "p,176\n");
                if (is_selected) {  //just previous_on_editor was selected.
                    delay(10);
                    this.on_wheels(0, 0, 0, 0, 1);
                    delay(10);
//                    keyboard.send_new("p,215\n");   //left . unselect.
                    send_ser_data(keyboard_o, "p,215\n");
                }
            } else {
//                keyboard.send_new("h,176\n");   //ENTER
//                 keyboard.send_new("h,129\n");   //LEFT SHIFT
                send_ser_data(keyboard_o, "h,129\n");
            }
        }

        if(c5==0 && on_hold) {
//             keyboard.send_new("r,129\n");   //LEFT SHIFT
            send_ser_data(keyboard_o, "r,129\n");
        }


        if (c5 == 1) {
            on_hold = true;
        } else {
            on_hold = false;
        }

        if (c1 != 0) {
            on_wheel_1(c1);
        } else if (c2 != 0) {
            on_wheel_2(c2);
        } else if (c3 != 0) {
            on_wheel_3(c3);
        }

        if(c1==0 && c2==0&& c3==0&& c5==0&& c6==0) {    //all is zero
            return;
        }
        if (!in_ribbon && c3==0) {
            if ((c1 != 0 || c2 != 0) && on_hold) {
                is_selected = true;
            } else {
                is_selected = false;
            }
        }

    }

    public void on_wheel_1(int val) {
        if (in_ribbon) {
            if (val == 1) {
//                keyboard.send_new("p,179\n");   //TAB
                send_ser_data(keyboard_o, "p,179\n");

            } else {
//                keyboard.send_new("h,129\n");   //LEFT SHIFT
//                keyboard.send_new("p,179\n");   //TAB
//                keyboard.send_new("r,129\n");   //LEFT SHIFT

                send_ser_data(keyboard_o, "h,129\n");
                send_ser_data(keyboard_o, "p,179\n");
                send_ser_data(keyboard_o, "r,129\n");

            }

            in_ribbon_nav = true;
            return;
        }
//        else if (on_hold) {
//            keyboard.send_new("h,129\n");   //LEFT SHIFT hold
//            if (val == 1) {
//                keyboard.send_new("p,215\n");   //left
//            } else if (val == -1) {
//                keyboard.send_new("p,216\n");   //right
//            }
//            keyboard.send_new("r,129\n");   //LEFT SHIFT release
//        }
        else {

            if (val == 1) {
//                keyboard.send_new("p,215\n");   //left
                send_ser_data(keyboard_o, "p,215\n");
            } else if (val == -1) {
//                keyboard.send_new("p,216\n");   //right
                send_ser_data(keyboard_o, "p,216\n");
            }
        }
    }

    public void on_wheel_2(int val) {
        System.out.println("on_wheel2=" + val);

        if (val == 1) {
//            keyboard.send_new("p,217\n");   //up
            send_ser_data(keyboard_o, "p,217\n");
        } else if (val == -1) {
//            keyboard.send_new("p,218\n");   //down
            send_ser_data(keyboard_o, "p,218\n");
        }
    }

    public void on_wheel_3(int val) {
        System.out.println("on_ribbon_wheel");
        if (!in_ribbon) {
            in_ribbon = true;
//            keyboard.send_new("p,130\n");     //left alt
//            keyboard.send_new("p,203\n");     //F10
            send_ser_data(keyboard_o, "p,203\n");
        }
        if (in_ribbon_nav) { //
//            keyboard.send_new("p,203\n");     //F10
            send_ser_data(keyboard_o, "p,203\n");
            delay(10);
//            keyboard.send_new("p,203\n");     //F10
            send_ser_data(keyboard_o, "p,203\n");
            in_ribbon_nav = false;
        }

        if (val > 0) {

//            keyboard.send_new("p,215\n");   //left
            send_ser_data(keyboard_o, "p,215\n");

        } else {
//            keyboard.send_new("p,216\n");   //right
            send_ser_data(keyboard_o, "p,216\n");
        }
    }

    public void beep() {
//        boolean s = ms.send_new("b\n");
        String send_str = "b\n";
        byte[] send_bytes = new byte[0];
        try {
            send_bytes = send_str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            wheel3d_o.write(send_bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("s=" + s);
    }

    public void send_haptic_beep(){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(mouse.x);
        System.out.println(screen.width);
        if (mouse.x < 5 || mouse.y < 5) {
            haptic();
            beep();
        } else if (mouse.x + 5 > screen.width || mouse.y + 5 > screen.height) {
            haptic();
            beep();
        }
//        delay(100);
    }

    public void haptic() {
//        boolean s = ms.send_new("h\n");
        String send_str = "h\n";
        byte[] send_bytes = new byte[0];
        try {
            send_bytes = send_str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            wheel3d_o.write(send_bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("s=" + s);
    }

    @Override
    public void onTTSCompleted() {
        System.out.println("Speak Completed");
    }

    class read_wheel3d extends Thread {

        public void run() {
            System.out.println("Device Reading started...");
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
                //enc,-1,0,0,0,0,0 //v2 data
//                String line = wheel3d.readLine2();
//                System.out.println("found=" + line);
                String line = "";
                int read_b_ASCII;
                do {
                    try {
                        read_b_ASCII = wheel3d_i.read();
                        line = line + (char)read_b_ASCII;
                    } catch (IOException e) {
                        read_b_ASCII = 10;
                        throw new RuntimeException(e);
                    }
                } while (read_b_ASCII != 10);
                System.out.println("found=" + line);

                String[] info = line.trim().split(",");
                int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0;
                curr_time = System.currentTimeMillis();
                long t_diff = curr_time - prev_time;
                if (info.length == 7) {
                    c1 = Integer.parseInt(info[1]);
                    c2 = Integer.parseInt(info[2]);
                    c3 = Integer.parseInt(info[3]);
                    c4 = Integer.parseInt(info[4]);
                    c5 = Integer.parseInt(info[5]);
                    c6 = Integer.parseInt(info[6]);

//                    System.out.println(t_diff);
                    if(t_diff > 100){
                        on_wheels(c1, c2, c3, c5, c6);
                    }
                }
                if(t_diff > 100){
                    send_haptic_beep();
                }
                prev_time = curr_time;
            }
        }

    }

    class read_keyboard extends Thread {

        public void run() {
            System.out.println("keyboardDevice Reading started...");
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
//                String line = keyboard.readLine2();
//                System.out.println("found=" + line);
                String line = "";
                int read_b_ASCII;
                do {
                    try {
                        read_b_ASCII = keyboard_i.read();
                        line = line + (char)read_b_ASCII;
                    } catch (IOException e) {
                        read_b_ASCII = 10;
                        throw new RuntimeException(e);
                    }
                } while (read_b_ASCII != 10);
                System.out.println("found=" + line);
            }
        }

    }

    public void init_keyhook() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent) {

                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {

                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {

//kc=41
//User typed: Back Quote
//kc=29
//User typed: Ctrl
                    int kc = nativeEvent.getKeyCode();
                    System.out.println("kc=" + kc);
                    String keyText = NativeKeyEvent.getKeyText(kc);
                    System.out.println("User typed: " + keyText);
//                    if (keyText.equals("Ctrl")) {
//                        System.out.println("__CTRL__");
//                        on_CTRL();
//                    }

                    if (kc == 41 && isMouseOnTild) {
                        on_CTRL();
                    } else if (kc == 29 && isMouseOnCTRL) {
                        on_CTRL();
                    }

                }
            });
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void on_CTRL() {
        if (isMouse || isMouse_sim) {
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            int xp = (int) ((100.0f * mouse.x) / screen.width);
            int yp = (int) ((100.0f * mouse.y) / screen.height);

//            DingDong sonyfication = new DingDong();
//            sonyfication.play_ding(1 + xp / 10);
//            sonyfication.play_dong(1 + yp / 10);
//            sonyfication.close();
            if (enableDingDong) {
                int xmin = config_xminfreq;
                int xmax = config_xmaxfreq;
                int ymin = config_yminfreq;
                int ymax = config_ymaxfreq;
                beep_ding_dong(xmin + xp * (xmax - xmin) / 100);
                delay(10);
                beep_ding_dong(ymin + yp * (ymax - ymin) / 100);

//                beep(300 + xp * 4);
//                delay(10);
//                beep(300 + yp * 4);
            }

            String txt = xp + "%," + yp + "%";
            System.out.println("speak=" + txt);

            if (enableTTS) {
                if (isTopLeft) {
                    speak_tts(xp + "% from left", 100 + xp);
                    speak_tts(yp + "% from top", 100 + yp);
                } else {
                    speak_tts(xp + "%", 100 + xp);
                    speak_tts(yp + "%", 100 + yp);
                }
            }

            //speak_windows(mouse.x + " , " + mouse.y);
            //speak_windows(txt);
//            speak_windows_percent(xp, yp);
        }
    }

    void beep_ding_dong(int freq) {

//        Toner.isplaying = false;
        try {
            //Toner.tone(freq, 500);
            double volume = config_tonevolume / 100.00;
            int toneduration = config_dingdongduration;
            Toner.tone(freq, toneduration, volume);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void speak_tts(String txt, int pitch) {
        tts.voice.setPitch(pitch);
        tts.speakText(txt);
    }

    public void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        new BridgeKeyboard();
    }

}

