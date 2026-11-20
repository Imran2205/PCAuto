package pcauto;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.util.prefs.Preferences;
import myarduinolib.ArduinoCmdClient;
import myarduinolib.SerialNR;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import simplenet.nsojib.github.io.*;
import simplerobotinterfacelib.TTSListener;
//import sonification.DingDong;
import sonification.Toner;
import ttsgui.TTS;
import webserversingle.EmuInterface;
import webserversingle.EmuServer;
import websocketj.WebSocketServerSingle;
import websocketj.WebsocketInterface;
import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import java.util.HashMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 *
 * @author FridayLab
 */
public class PCAuto implements WebsocketInterface, EmuInterface, SimpleNetServerListener, TTSListener {

    GUI g;
    Robot robot;
//    static final String TAG = ArduinoCmdClient.class.getSimpleName();
//    private String port = "COM18";
//    private int baud = 230400;
    private int baud = 9600;
    private SerialPort ms_d;
    private SerialPort hw_mouse_d;
    private InputStream ms_i;
    private OutputStream ms_o;
    private InputStream hw_mouse_i;
    private OutputStream hw_mouse_o;
    private boolean isReady = false;
    int wh = 0, wh2 = 0, wh3 = 0;

    int c1t = 0, c2t = 0, c3t = 0;

    WebSocketServerSingle ws;
    boolean wheel_23_arrow_key_bound = false;
    boolean shift_hold = false;

    String local_ip = "localhost";

    public boolean native_bind = false; //native arrow key

    SimpleNetServer server;
    public static final int PORT_SOCKET = 4455;

    public boolean useAsMouse = false;
    public int mouse_speed = 3;

    //hw mouse.


    int ct = 0;
    boolean c1_b, c2_b, c3_b;
    boolean is_vsto = true;
    Dimension screen;

    TTS tts;
    Preferences prefs;
    Toner toner;

    /*
    work as a websocket server for the Wheel3D device.
     */
    public PCAuto() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        toner = new Toner();

        g = new GUI(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> send_dev("a")));
        System.out.println(this.tts);
        System.out.println(g.leader.tts);
        g.setVisible(true);
        g.setLocationRelativeTo(null);
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            local_ip = inetAddress.getHostAddress();
            System.out.println("IP Address: " + local_ip);
            g.add_txt("Local Server IP: " + local_ip);

            robot = new Robot();

            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println(Arrays.toString(ports));

//            System.out.println(Arrays.toString(SerialNR.getPorts()));

//            String[] ports = SerialNR.getPorts();

            System.out.println(Arrays.toString(ports));

            g.setPortList(ports);
        } catch (Exception ex) {
            System.out.println("Error=" + ex.getMessage());
            g.add_txt("Error=" + ex.getMessage());
        }

        System.out.println("Starting emulator server");
        try {
            EmuServer emu = new EmuServer(this, 80);
            emu.start();
        } catch (Exception ex) {
            System.out.println("Emulator server can't start");
        }

        System.out.println("starting websocket server on port=" + 8000);
        g.add_txt("starting websocket server on port=" + 8000);
        try {
            ws = new WebSocketServerSingle(this, 8000);
            ws.start();
        } catch (Exception ex) {
        }

        //
        g.add_txt("Starting local socket server");
        try {
            server = new SimpleNetServer(0, 4455, this);
            System.out.println("server created");
            g.add_txt("server created. waiting for client...");
            new Thread() {
                public void run() {
                    server.runAllDayLong();
                }
            }.start();
        } catch (Exception ex) {
            g.add_txt("socket error: " + ex.getMessage());
        }

        screen = Toolkit.getDefaultToolkit().getScreenSize();

//        sonyfication = new DingDong();
//        sonyfication.play_ding(2);
//        delay(30);
//        sonyfication.play_dong(5);
        init_keyhook();
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        tts = new TTS(this);
        System.out.println(">>>>"+tts);
        tts.speakText("Ready");
    }

    public void speak_tts(String txt, int pitch) {
        tts.voice.setPitch(pitch);
        tts.speakText(txt);
    }

    @Override
    public void onTTSCompleted() {
        System.out.println("Speak Completed");
    }

    public void onTestButton(int a, int b) {
//        DingDong sonyfication = new DingDong();
//        sonyfication.play_ding(a);
////        delay(400);
////        sonyfication.play_dong(b);
//        sonyfication.close();
    }

    public void on_CTRL() {
        if (g.isMouse() || g.isMouse_sim()) {
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            int xp = (int) ((100.0f * mouse.x) / screen.width);
            int yp = (int) ((100.0f * mouse.y) / screen.height);

//            DingDong sonyfication = new DingDong();
//            sonyfication.play_ding(1 + xp / 10); 
//            sonyfication.play_dong(1 + yp / 10);
//            sonyfication.close();
            if (g.enableDingDong()) {
                int xmin = g.config.get("xminfreq");
                int xmax = g.config.get("xmaxfreq");
                int ymin = g.config.get("yminfreq");
                int ymax = g.config.get("ymaxfreq");
                beep_ding_dong(xmin + xp * (xmax - xmin) / 100);
                delay(10);
                beep_ding_dong(ymin + yp * (ymax - ymin) / 100);

//                beep(300 + xp * 4);
//                delay(10);
//                beep(300 + yp * 4);
            }

            String txt = xp + "%," + yp + "%";
            System.out.println("speak=" + txt);

            if (g.enableTTS()) {
                if (g.isTopLeft()) {
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

    Toner t;

    void beep(int freq) {

//        Toner.isplaying = false;
        try {
            //Toner.tone(freq, 500); 
            double volume = g.config.get("tonevolume") / 100.00;
            int toneduration = g.config.get("toneduration");
            //Toner.tone(freq, toneduration, volume);

            // toner.stop();
            //toner.playTone(freq, toneduration, volume);
            //toner.start_tone(freq, toneduration, volume);
            if (t != null) {
                t.stopPlay();
            }
            t = new Toner();
            t.setData(freq, toneduration, volume);
            t.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void beep_ding_dong(int freq) {

//        Toner.isplaying = false;
        try {
            //Toner.tone(freq, 500); 
            double volume = g.config.get("tonevolume") / 100.00;
            int toneduration = g.config.get("dingdongduration");
            Toner.tone(freq, toneduration, volume);

        } catch (Exception ex) {
            ex.printStackTrace();
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

                    if (kc == NativeKeyEvent.VC_M && (nativeEvent.getModifiers() & NativeKeyEvent.META_MASK) != 0) {
                        toggle_mode();
                        return;
                    }

                    if (kc == 41 && g.isMouseOnTild()) {
                        on_CTRL();
                    } else if (kc == 29 && g.isMouseOnCTRL()) {
                        on_CTRL();
                    }

                }
            });
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void connect_arduino_mouse(String port) {
        try {
            System.out.println("Connecting with arduino_cmd_server on port " + port + " at " + baud + " rate");
            g.add_txt("Connecting with arduino_cmd_server on port " + port + " at " + baud + " rate");
//            hw_mouse = new SerialNR(port, baud);
//            ms.disDebug();
//            hw_mouse_d = SerialPort.getCommPort(port);
//            hw_mouse_d.setComPortParameters(baud, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
//            hw_mouse_d.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
//            hw_mouse.enDebug();
//            var hasOpened = hw_mouse_d.openPort();
//            if(!hasOpened){
//                System.out.println("Failed to open serial port.");
//            }
//            hw_mouse_i = hw_mouse_d.getInputStream();
//            hw_mouse_o = hw_mouse_d.getOutputStream();
            hw_mouse_d = ms_d;
            hw_mouse_o = ms_o;
            hw_mouse_i = ms_i;
            System.out.println("Connected");
        } catch (Exception ex) {
            g.setTitle(g.getTitle() + "_Not Connected");
            System.out.println("Can't connect");
//            ex.printStackTrace();
        }
    }

    public void connect_arduino(String port) {
        try {
            System.out.println("Connecting with arduino_cmd_server on port " + port + " at " + baud + " rate");
            g.add_txt("Connecting with arduino_cmd_server on port " + port + " at " + baud + " rate");
//            ms = new SerialNR(port, baud);
            ms_d = SerialPort.getCommPort(port);
            ms_d.setComPortParameters(9600, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            ms_d.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
//            ms.disDebug();
//            ms.enDebug();
            var hasOpened = ms_d.openPort();
            if(!hasOpened){
                System.out.println("Failed to open serial port.");
            }
            ms_i = ms_d.getInputStream();
            ms_o = ms_d.getOutputStream();
            System.out.println("Connected");
            send_dev("a");  //clear any keys left held from a previous session

//            System.out.println("Waiting for ready.");
            checkReady();
            g.setTitle(g.getTitle() + "_Connected");
        } catch (Exception ex) {
            g.setTitle(g.getTitle() + "_Not Connected");
            System.out.println("Can't connect");
//            ex.printStackTrace();
        }
        new read_arduino().start();
    }

    class read_arduino extends Thread {

        public void run() {
            System.out.println("Device Reading started...");
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
                //enc,-1,0,0,0,0,0 //v2 data
//                String line = ms.readLine2();
                String line = "";
                int read_b_ASCII;
                do {
                    try {
                        read_b_ASCII = ms_i.read();
                        line = line + (char)read_b_ASCII;
                    } catch (IOException e) {
                        read_b_ASCII = 10;
                        throw new RuntimeException(e);
                    }
                } while (read_b_ASCII != 10);
                System.out.println("found=" + line);
                int[] v = parse_wheeler_line(line);
                if (v == null) {
                    g.add_txt(line.trim());  //not wheel/button data (e.g. "Ready")
                    continue;
                }
                on_device_event(line.trim(), v);
            }
        }

    }

    /*
    Device line formats:
      enc,w1,w2,w3,b1,b2,both,long1,long2   (current firmware)
      enc,w1,w2,w3,x,click,back             (older firmware / emulator)
    w1..w3: +1 upward turn, -1 downward turn, 0 none
    b1, b2: 1 while the button is held; both: 1 from both pressed until both are released
    long1, long2: 1 in the line where a 1 s long press is recognised
    Returns {w1, w2, w3, b1, b2, both, long1, long2}, or null for non-data lines.
     */
    static int[] parse_wheeler_line(String line) {
        String[] info = line.trim().split(",");
        if (!info[0].equals("enc")) {
            return null;
        }
        try {
            if (info.length == 9) {
                int[] v = new int[8];
                for (int i = 0; i < 8; i++) {
                    v[i] = Integer.parseInt(info[i + 1].trim());
                }
                return v;
            } else if (info.length == 7) {
                return new int[]{Integer.parseInt(info[1].trim()), Integer.parseInt(info[2].trim()), Integer.parseInt(info[3].trim()),
                    Integer.parseInt(info[5].trim()), Integer.parseInt(info[6].trim()), 0, 0, 0};
            }
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public void on_device_event(String data, int[] v) {
        System.out.println("on_device_event=" + data);
        g.add_txt(data);

        int w1 = v[0], w2 = v[1], w3 = v[2];
        if (useAsMouse) {
            // Device sends +1 for an upward wheel turn. Mouse mapping:
            // wheel 1: + up / - down, wheel 2: + left / - right, wheel 3: + faster / - slower
            this.parse_device_for_mouse(-w1, -w2, -w3);
        } else {
            if (ws != null && ws.isConnected()) {
                ws.send_msg(data);
            }
            keyboard_wheel(w1, "kb_w1_pos", "kb_w1_neg");
            keyboard_wheel(w2, "kb_w2_pos", "kb_w2_neg");
            keyboard_wheel(w3, "kb_w3_pos", "kb_w3_neg");
        }
        handle_buttons(v[3], v[4], v[5], v[6], v[7]);
    }

    // ---- Keyboard mode: the device presses the keys over USB ----

    private void keyboard_wheel(int step, String pos_slot, String neg_slot) {
        if (step > 0) {
            key_tap(g.role(pos_slot));
        } else if (step < 0) {
            key_tap(g.role(neg_slot));
        }
    }

    // Button events from the raw button / both / long-press values.
    // short press = released without a long press and without being part of a both-button press
    private int prev_b1 = 0, prev_b2 = 0;
    private boolean b1_long = false, b2_long = false, b1_both = false, b2_both = false;
    private boolean both_active = false;
    private String both_action = null;
    private String lock1_action = null, lock2_action = null;  //actions held on by a long press

    private void handle_buttons(int b1, int b2, int both, int long1, int long2) {
        if (b1 == 1 && prev_b1 == 0) {
            b1_long = false;
            b1_both = both_active;  //re-pressed during a both-button press: still part of it
        }
        if (b2 == 1 && prev_b2 == 0) {
            b2_long = false;
            b2_both = both_active;
        }

        if (both == 1 && !both_active) {
            both_active = true;
            b1_both = true;
            b2_both = true;
            both_action = g.role("kb_both");
            g.add_txt("both buttons: hold " + both_action);
            key_hold(both_action);
        } else if (both == 0 && both_active) {
            both_active = false;
            g.add_txt("both buttons: release " + both_action);
            key_release(both_action);
            both_action = null;
        }

        if (long1 == 1 && !b1_both) {
            b1_long = true;
            lock1_action = toggle_lock(lock1_action, g.role("kb_b1_long"), "button 1");
        }
        if (long2 == 1 && !b2_both) {
            b2_long = true;
            lock2_action = toggle_lock(lock2_action, g.role("kb_b2_long"), "button 2");
        }

        if (b1 == 0 && prev_b1 == 1 && !b1_long && !b1_both) {
            if (useAsMouse) {
                send_dev("l");
            } else {
                key_tap(g.role("kb_b1_short"));
            }
        }
        if (b2 == 0 && prev_b2 == 1 && !b2_long && !b2_both) {
            if (useAsMouse) {
                send_dev("r");
            } else {
                key_tap(g.role("kb_b2_short"));
            }
        }
        prev_b1 = b1;
        prev_b2 = b2;
    }

    // Long press: first one holds the action down, the next one releases it.
    // The device buzzes when a long press is recognised; a beep is added when the hold turns on.
    private String toggle_lock(String current, String action, String name) {
        if (current != null) {
            g.add_txt(name + " long press: release " + current);
            key_release(current);
            return null;
        }
        if (WheelerKeys.NONE.equals(action)) {
            return null;
        }
        g.add_txt(name + " long press: hold " + action);
        key_hold(action);
        send_dev("b");
        return action;
    }

    // Number of active holds per key code, so overlapping holds (e.g. Shift lock + Shift+Tab)
    // only release a key when nothing else is holding it.
    private final HashMap<Integer, Integer> held = new HashMap<>();

    private synchronized void key_hold(String action) {
        for (int code : WheelerKeys.codes(action)) {
            int n = held.getOrDefault(code, 0);
            if (n == 0) {
                send_dev("h," + code);
            }
            held.put(code, n + 1);
        }
    }

    private synchronized void key_release(String action) {
        int[] codes = WheelerKeys.codes(action);
        for (int i = codes.length - 1; i >= 0; i--) {
            int n = held.getOrDefault(codes[i], 0);
            if (n <= 1) {
                held.remove(codes[i]);
                if (n == 1) {
                    send_dev("r," + codes[i]);
                }
            } else {
                held.put(codes[i], n - 1);
            }
        }
    }

    private synchronized void key_tap(String action) {
        int[] codes = WheelerKeys.codes(action);
        if (codes.length == 0) {
            return;
        }
        for (int code : codes) {
            if (!held.containsKey(code)) {
                send_dev("h," + code);
            }
        }
        for (int i = codes.length - 1; i >= 0; i--) {
            if (!held.containsKey(codes[i])) {
                send_dev("r," + codes[i]);
            }
        }
    }

    // Releases every key the device is holding (also clears long-press holds)
    public synchronized void release_all_keys() {
        held.clear();
        lock1_action = null;
        lock2_action = null;
        both_active = false;
        both_action = null;
        send_dev("a");
        if (g != null) {
            g.add_txt("all keys released");
        }
    }

    // Cmd+M: switch between mouse mode and keyboard mode
    public void toggle_mode() {
        onUseAsMouse(!useAsMouse);
        g.set_mouse_mode(useAsMouse);
        String name = useAsMouse ? "Mouse mode" : "Keyboard mode";
        g.add_txt(name);
        if (tts != null) {
            tts.speakText(name);
        }
    }

    private synchronized void send_dev(String cmd) {
        if (ms_o == null) {
            return;
        }
        try {
            ms_o.write((cmd + "\n").getBytes("US-ASCII"));
            ms_o.flush();
        } catch (IOException e) {
            System.out.println("send failed: " + e.getMessage());
        }
    }

    void simulate_key(int key_code) {
        try {
            robot.keyPress(key_code);
            Thread.sleep(10);
            robot.keyRelease(key_code);
        } catch (Exception ex) {
        }
    }

    int lt = 0;
    int ls = 0;
    int lf = 0;

    public void onChange(String data) {
        String[] info = data.trim().split(",");
        int f = Integer.parseInt(info[1]);
        int s = Integer.parseInt(info[2]);
        int t = Integer.parseInt(info[3]);

        if (t > lt) {
            robot.keyPress(KeyEvent.VK_UP);
        } else if (t < lt) {
            robot.keyPress(KeyEvent.VK_DOWN);
        }

        if (s > ls) {
            robot.keyPress(KeyEvent.VK_LEFT);
        } else if (s < ls) {
            robot.keyPress(KeyEvent.VK_RIGHT);
        }

        if (f > lf) {
            leftClickDown(true);
        } else if (f < lf) {
            robot.keyPress(KeyEvent.VK_CONTEXT_MENU);
        }

        lt = t;
        ls = s;
        lf = f;
    }

    public static void leftClickDown(boolean down) {
        try {
            Robot robot = new Robot();
            if (down) {
                robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
            } else {
                robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
            }
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void rightClick() {
        try {
            Robot robot = new Robot();
            robot.mousePress(MouseEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(MouseEvent.BUTTON3_DOWN_MASK);

        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void checkReady() {
//        Vector<String> lines = ms.readResponse();
        isReady = true;

//        String line = "";
//        int read_b_ASCII;
//        do {
//            try {
//                read_b_ASCII = ms_i.read();
//                line = line + (char)read_b_ASCII;
//            } catch (IOException e) {
//                read_b_ASCII = 10;
//                throw new RuntimeException(e);
//            }
//        } while (read_b_ASCII != -1);

//        for (String line : lines) {
//        if (line.toLowerCase().contains("ready")) {
//            isReady = true;
//            System.out.println("IS Ready");
//            System.out.println("isready");
            return;
//        }
//        }
    }

    public void on_native_bind(boolean s) {
        System.out.println("native_bind=" + s);
        this.native_bind = s;
    }

    public void onUseAsMouse(boolean state) {
        useAsMouse = state;
    }

    public void onButton(String name) {
        System.out.println("name=" + name);
        g.add_txt("onButton=" + name);
        if (name.equals("init_vsto")) {
            server.send("tab\n");
            is_vsto = true;
            return;
        }

        if (ws.isConnected()) {
            ws.send_msg("gui=" + name);
        }

        if (useAsMouse) {
            this.parse_UI_button_for_mouse(name);
            return;
        }

//        ms.enDebug();
//        if (name.equals("up")) {
//            beep();
//        } else if (name.equals("down")) {
//            haptic();
//        }
        if (name.equals("wu1")) {
            server.send("gnext1\n");         //add g to let know sent from GUI, not wheeler3
        } else if (name.equals("wd1")) {
            server.send("gprev1\n");
        } else if (name.equals("wu2")) {
            server.send("gnext2\n");
        } else if (name.equals("wd2")) {
            server.send("gprev2\n");
        } else if (name.equals("wu3")) {
            server.send("gnext\n");
        } else if (name.equals("wd3")) {
            server.send("gprev\n");
        } else if (name.equals("click")) {
            server.send("click\n");
        } else if (name.equals("back")) {
            server.send("back\n");
        }

//        } 
//        if (name.equals("up")) {
//            robot.keyPress(KeyEvent.VK_UP);
//        } else if (name.equals("down")) {
//            robot.keyPress(KeyEvent.VK_DOWN);
//        }
    }

    public void speak_windows(String txt) {
        server.send("tts=" + txt + "\n");
        // tts.speakTextNb(txt);
    }

    public void speak_windows_percent(int x, int y) {
        server.send("ttsp=" + x + "," + y + "\n");
    }

    private void parse_UI_button_for_mouse(String name) {
        Point mouse = MouseInfo.getPointerInfo().getLocation();

        if (name.equals("wu1")) {
            robot.mouseMove(mouse.x + mouse_speed, mouse.y);
        } else if (name.equals("wd1")) {
            robot.mouseMove(mouse.x - mouse_speed, mouse.y);
        } else if (name.equals("wu2")) {
            robot.mouseMove(mouse.x, mouse.y + mouse_speed);
        } else if (name.equals("wd2")) {
            robot.mouseMove(mouse.x, mouse.y - mouse_speed);
        } else if (name.equals("wu3")) {
            mouse_speed++;
            g.update_speed(mouse_speed);
        } else if (name.equals("wd3")) {
            mouse_speed--;
            g.update_speed(mouse_speed);
        } else if (name.equals("click")) {
            leftClickDown(true);
        } else if (name.equals("back")) {
            rightClick();
        }
    }

    /*
    Hardware mouse.
     */
    // c1 -> vertical ("y", + is down), c2 -> horizontal ("x", + is right), c3 -> mouse speed
    // (button clicks are handled in handle_buttons)
    private void parse_device_for_mouse(int c1, int c2, int c3) {
        Point mouse = MouseInfo.getPointerInfo().getLocation();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int wheel = 0;
//        System.out.println("C1="+c1);
        if (c1 == 1) {
            //robot.mouseMove(mouse.x, mouse.y + mouse_y_speed);
            wheel = 1;
//            hw_mouse.send_new("y," + mouse_speed + "\n");
            String send_str = "y," + mouse_speed + "\n";
            System.out.println(send_str);
            byte[] send_bytes = new byte[0];
            try {
                send_bytes = send_str.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            try {
                hw_mouse_o.write(send_bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (c1 == -1) {
            wheel = 1;
//            hw_mouse.send_new("y," + (-mouse_speed) + "\n");
            String send_str = "y," + (-mouse_speed) + "\n";
            byte[] send_bytes = new byte[0];
            try {
                send_bytes = send_str.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            try {
                hw_mouse_o.write(send_bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (c2 == 1) {
            wheel = 2;
//            hw_mouse.send_new("x," + mouse_speed + "\n");
            String send_str = "x," + mouse_speed + "\n";
            byte[] send_bytes = new byte[0];
            try {
                send_bytes = send_str.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            try {
                hw_mouse_o.write(send_bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (c2 == -1) {
            wheel = 2;
//            hw_mouse.send_new("x," + (-mouse_speed) + "\n");
            String send_str = "x," + (-mouse_speed) + "\n";
            byte[] send_bytes = new byte[0];
            try {
                send_bytes = send_str.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            try {
                hw_mouse_o.write(send_bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        int dx = c2 > 0 ? 1 : (c2 < 0 ? -1 : 0);
        int dy = c1 > 0 ? 1 : (c1 < 0 ? -1 : 0);
        check_edge_alert(mouse, screen, dx, dy);

        if (c3 == 1) {
            mouse_speed--;
            g.update_speed(mouse_speed);
            return;
        } else if (c3 == -1) {
            mouse_speed++;
            g.update_speed(mouse_speed);
            return;
        }

//         System.out.println("mousespeak="+g.isMouseSpeak());
        if (g.isMouseLocSpeak()) {
            //speak_windows("mouse " + mouse.x + " , " + mouse.y);
//            System.out.println("dddddd");
            on_CTRL();
        }

        if (g.isWheelSoundEnable()) {
            int xmin = g.config.get("xminfreq");
            int xmax = g.config.get("xmaxfreq");
            int ymin = g.config.get("yminfreq");
            int ymax = g.config.get("ymaxfreq");
            if (wheel == 2) {
                int xp = (int) ((100.0f * mouse.x) / screen.width);
                beep(xmin + xp * (xmax - xmin) / 100);
            } else if (wheel == 1) {
                int yp = (int) ((100.0f * mouse.y) / screen.height);
                beep(ymin + yp * (ymax - ymin) / 100);
            }
        }

    }

    // Screen-edge alert: beep + haptic once per push toward an edge, instead of on every wheel step.
    // A new alert is only sent after the user moves away from the edge or pauses, so the device
    // is not flooded with blocking beep/haptic commands while the cursor sits at the edge.
    private static final int EDGE_PX = 5;
    private static final long EDGE_ALERT_MIN_INTERVAL_MS = 400;
    private static final long EDGE_PUSH_RESET_MS = 600;
    private boolean edge_alerted = false;
    private long last_edge_alert_ms = 0;
    private long last_edge_move_ms = 0;

    private void check_edge_alert(Point mouse, Dimension screen, int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        // Position after this step (the device moves the cursor after we send x/y)
        int nx = mouse.x + dx * mouse_speed;
        int ny = mouse.y + dy * mouse_speed;
        boolean left = nx < EDGE_PX;
        boolean right = nx + EDGE_PX > screen.width;
        boolean top = ny < EDGE_PX;
        boolean bottom = ny + EDGE_PX > screen.height;
        boolean toward_edge = (left && dx < 0) || (right && dx > 0) || (top && dy < 0) || (bottom && dy > 0);

        long now = System.currentTimeMillis();
        if (now - last_edge_move_ms > EDGE_PUSH_RESET_MS) {
            edge_alerted = false;  //paused: next push toward the edge counts as a new one
        }
        last_edge_move_ms = now;

        if (!toward_edge) {
            edge_alerted = false;  //moving away from (or not near) the edge: no alert
            return;
        }
        if (!edge_alerted && now - last_edge_alert_ms > EDGE_ALERT_MIN_INTERVAL_MS) {
            edge_alerted = true;
            last_edge_alert_ms = now;
            haptic();
            beep();
        }
    }

    private void parse_device_for_mouse_robot333(int c1, int c2, int c3, int c4, int c5, int c6) {
        Point mouse = MouseInfo.getPointerInfo().getLocation();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        if (c1 == 1) {
            robot.mouseMove(mouse.x, mouse.y + mouse_speed);
        } else if (c1 == -1) {
            robot.mouseMove(mouse.x, mouse.y - mouse_speed);
        }
        if (c2 == 1) {
            robot.mouseMove(mouse.x + mouse_speed, mouse.y);
        } else if (c2 == -1) {
            robot.mouseMove(mouse.x - mouse_speed, mouse.y);
        }

        if (c3 == 1) {
            mouse_speed--;
            g.update_speed(mouse_speed);
        } else if (c3 == -1) {
            mouse_speed++;
            g.update_speed(mouse_speed);
        } else if (c5 == 1) {
            leftClickDown(true);
            delay(30);
            leftClickDown(false);
        } else if (c6 == 1) {
            rightClick();
        }

        if (mouse.x < 5 || mouse.y < 5) {
            haptic();
            beep();
        } else if (mouse.x + 5 > screen.width || mouse.y + 5 > screen.height) {
            haptic();
            beep();
        }

    }




    void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {
        }
    }

    public void send_haptic_beep(){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
//        System.out.println(mouse.x);
//        System.out.println(screen.width);
        if (mouse.x < 5 || mouse.y < 5) {
            haptic();
            beep();
        } else if (mouse.x + 5 > screen.width || mouse.y + 5 > screen.height) {
            haptic();
            beep();
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
            ms_o.write(send_bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("s=" + s);
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
            ms_o.write(send_bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("s=" + s);
    }

    public static void main(String[] args) {
        new PCAuto();
    }

    @Override
    public void onopen(String remote) {
        System.out.println("onopen=" + remote);
        g.add_txt("onopen=" + remote);
    }

    @Override
    public void onmessage(String msg) {
        System.out.println("onmessage=" + msg);
        if (msg.startsWith("req_")) {
            msg = msg.replace("req_", "");
        }
        g.add_txt("onmessage=" + msg);

        if (msg.contains("arrow_bind")) {
            wheel_23_arrow_key_bound = true;
        } else if (msg.contains("arrow_unbind")) {
            wheel_23_arrow_key_bound = false;
        } else if (msg.contains("click")) {

            if (msg.indexOf(",") > 0) {
                String[] inf = msg.split(",");
                int x = Integer.parseInt(inf[1]);
                int y = Integer.parseInt(inf[2]);
                robot.mouseMove(x + 40, y + 50);
                mouse_click();
                System.out.println("click request on=" + x + " " + y);
                g.add_txt("click request on=" + x + " " + y);
            } else {
                mouse_click();
                g.add_txt("click simulated");
//                simulate_key(KeyEvent.VK_ENTER);
//
//                g.add_txt("_enter_pressed_");
            }

        } else if (msg.contains("enter")) {
            simulate_key(KeyEvent.VK_ENTER);
            g.add_txt("_enter_pressed_");
        } else if (msg.contains("shift")) {
            robot.keyPress(KeyEvent.VK_SHIFT);
//           robot.keyRelease(KeyEvent.VK_SHIFT);
            shift_hold = true;
        } else if (msg.contains("move")) {
            //move,421,264
            String[] inf = msg.split(",");
            int x = Integer.parseInt(inf[1]);
            int y = Integer.parseInt(inf[2]);
            robot.mouseMove(x, y);
            System.out.println("move request on=" + x + " " + y);
            g.add_txt("move request on=" + x + " " + y);
//                
//           robot.keyPress(KeyEvent.CTRL_DOWN_MASK);
//           robot.keyPress(KeyEvent.VK_C);
//           robot.keyRelease(KeyEvent.CTRL_DOWN_MASK);
//           
//                

        } else if (msg.contains("unhold")) {
            robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
        } else if (msg.contains("hold")) {
            String[] inf = msg.split(",");
            int x = Integer.parseInt(inf[1]);
            int y = Integer.parseInt(inf[2]);
            robot.mouseMove(x, y);
            robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
            System.out.println("hold request on=" + x + " " + y);
            g.add_txt("hold request on=" + x + " " + y);

            //+unbind arraw.
            //wheel_23_arrow_key_bound = false;
        }
    }

    void mouse_click() {
        try {
            robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(10);
            robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onclose() {
        System.out.println("websocket:connection closed");
        g.add_txt("onclose");
    }

    @Override
    public void on_emu_data(String line) {
        System.out.println("emu_found=" + line);
        int[] v = parse_wheeler_line(line);
        if (v != null) {
            on_device_event(line.trim(), v);
        }
    }

    boolean has_client = false;

    @Override
    public void newSimpleMsg(int serverId, String line) {
        System.out.println("msg=" + line);
        g.add_txt("socket: " + line);
        if (line.startsWith("__NO_")) {
            do_warn();
        }

    }

    public void do_warn() {
        if (g.warning_hapic_beep() == 1) {
            beep();
        } else if (g.warning_hapic_beep() == 2) {
            haptic();
        } else {
            beep();
            haptic();
        }
    }

    @Override
    public void onSimpleDisconnect(int serverId) {
        System.out.println("disconnected");
        has_client = false;
        g.add_txt("socket: disconnected");
    }

    @Override
    public void newSimpleClient(int serverId, String ip) {
        System.out.println("newclient=" + ip);
        has_client = true;
        g.add_txt("socket: new connection: " + ip);
    }
}
