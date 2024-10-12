package wheelerdriver;

import arduino2bridge.bridgekeyboard;
import com.fazecast.jSerialComm.SerialPort;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import wheelerdriver.WheelerDriver;
import simplenet.nsojib.github.io.SimpleNetServer;
import simplenet.nsojib.github.io.SimpleNetServerListener;
import simplerobotinterfacelib.TTSListener;
import sonification.Toner;
import ttsgui.TTS;
import webserversingle.EmuInterface;
import webserversingle.EmuServer;
import websocketj.WebSocketServerSingle;
import websocketj.WebsocketInterface;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.prefs.Preferences;
import wheelerdriver.CtrlKeyChecker;

public class WheelerDriver implements WebsocketInterface, EmuInterface, SimpleNetServerListener, TTSListener {

    GUI g;
    Robot robot;
    //    static final String TAG = ArduinoCmdClient.class.getSimpleName();
//    private String port = "COM18";
//    private int baud = 230400;
    private static boolean ctrlPressed = false;
    private int baud = 9600;
    private SerialPort ms_d;
    private SerialPort hw_mouse_d;
    private InputStream ms_i;
    private OutputStream ms_o;
    private InputStream hw_mouse_i;
    private OutputStream hw_mouse_o;

    private SerialPort wheel3d, keyboard;
    private InputStream wheel3d_i, keyboard_i;
    private OutputStream wheel3d_o, keyboard_o;

    private long prev_time = System.currentTimeMillis();
    private long curr_time = System.currentTimeMillis();

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

    boolean in_ribbon = false;
    boolean in_ribbon_nav = false;
    boolean on_hold = false;
    boolean is_selected = false;
    boolean first_boundary_hit = true;

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
    public WheelerDriver() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        toner = new Toner();

        g = new GUI(this);
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
                public void nativeKeyReleased(NativeKeyEvent e) {
                    if (e.getKeyCode() == 29) {
                        ctrlPressed = false;
                    }
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent) {

                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {

//kc=41
//User typed: Back Quote
//kc=29
//User typed: Ctrl
                    if (nativeEvent.getKeyCode() == 29) {
                        ctrlPressed = true;
                    }

                    int kc = nativeEvent.getKeyCode();
                    System.out.println("kc=" + kc);
                    String keyText = NativeKeyEvent.getKeyText(kc);
                    System.out.println("User typed: " + keyText);
//                    if (keyText.equals("Ctrl")) {
//                        System.out.println("__CTRL__");
//                        on_CTRL();
//                    }

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

            keyboard = ms_d;
            keyboard_i = hw_mouse_i;
            keyboard_o = hw_mouse_o;

            wheel3d = keyboard;
            wheel3d_i = keyboard_i;
            wheel3d_o = keyboard_o;

            System.out.println("Connected");
            g.setTitle(g.getTitle() + "_Connected");
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

//            System.out.println("Waiting for ready.");
            checkReady();
            g.setTitle(g.getTitle() + "_Connected");
        } catch (Exception ex) {
            g.setTitle(g.getTitle() + "_Not Connected");
            System.out.println("Can't connect");
//            ex.printStackTrace();
        }
        new WheelerDriver.read_arduino().start();
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
                String[] info = line.trim().split(",");
                int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0, c7 = 0;
                if (info.length == 8) {
                    first_boundary_hit = true;
                    c1 = Integer.parseInt(info[1]);
                    c2 = Integer.parseInt(info[2]);
                    c3 = Integer.parseInt(info[3]);
                    c4 = Integer.parseInt(info[4]);
                    c5 = Integer.parseInt(info[5]);
                    c6 = Integer.parseInt(info[6]);
                    c7 = Integer.parseInt(info[7]);

                    if (c1 != 0) {
                        c1_b = !c1_b;
                    }
                    if (c2 != 0) {
                        c2_b = !c2_b;
                    }
                    if (c3 != 0) {
                        c3_b = !c3_b;
                    }
                    System.out.println("c1c2c3_b" + c1_b + "," + c2_b + "," + c3_b);

                    c1t += c1;
                    c2t += c2;
                    c3t += c3;

                    if (Math.abs(c1t) == 24) { //speed reduce?
                        wh += c1;
                        c1t = 0;
                    }
                    if (Math.abs(c2t) == 12) {
                        wh2 += c2;
                        c2t = 0;
                    }
                    if (Math.abs(c3t) == 48) {
                        wh3 += c3;
                        c3t = 0;
                    }
                    String recreate = "enc," + wh + "," + wh2 + "," + wh3 + "," + c4 + "," + c5 + "," + c6 + "," + c7;
//                    line = recreate;
                    System.out.println("found_recreated=" + line);

                }
                //older version

                curr_time = System.currentTimeMillis();
                long t_diff = curr_time - prev_time;
//                    System.out.println(t_diff);
                if(t_diff > 50){
                    on_device_event(line, c1, c2, c3, c4, c5, c6, c7);
                }
                prev_time = curr_time;

//                on_device_event(line, c1, c2, c3, c4, c5, c6);
            }
        }

    }

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

    public void on_device_event(String data, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
        System.out.println("on_device_event=" + data);
        g.add_txt(data);

//        if (ctrlPressed) {
//            System.out.println("Ctrl is currently pressed");
//        }

        if (c7 == 1) {
            System.out.println("Change mode");
//            ctrlPressed
//            if (true) {
            System.out.println("Ctrl is currently pressed");
            useAsMouse = !useAsMouse;
            if (useAsMouse) {
                tts.speakText("2D nav mode");
            }
            else{
                tts.speakText("H nav mode");
            }
//            }
            return;
        }

        if (useAsMouse) {
            this.parse_device_for_mouse(c2, c1, c3, c4, c5, c6);
            return;
        }

        if (is_vsto) {
            System.out.println(">>>>> is_vsto");
            //office works.

//            System.out.println("c3=" + c3);
            if (c3 == 1) {
                server.send("next1\n");
            } else if (c3 == -1) {
                server.send("prev1\n");
            }
            if (c2 == 1) {
                server.send("next2\n");
            } else if (c2 == -1) {
                server.send("prev2\n");
            }
            if (c1 == 1) {
                server.send("next\n");
            } else if (c1 == -1) {
                server.send("prev\n");
            } else if (c5 == 1) {
                server.send("click\n");
            } else if (c6 == 1) {
                server.send("back\n");
            }

            return;
        }

        if (ws != null && ws.isConnected()) {
            System.out.println(">>>>> ws_connected");
            ws.send_msg(data);
        }

        if (c6 == 1) {
            System.out.println(">>>>> c6==1");
            System.out.println("Simulating escape key");
            robot.keyPress(KeyEvent.VK_ESCAPE);
        }

        if (c5 == 0) {
            System.out.println(">>>>> c5==0");
            shift_hold = false;  //release shift hold
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }

        if (wheel_23_arrow_key_bound || native_bind) {
            System.out.println(">>>>> wheel_23_arrow_key_bound || native_bind");
            if (c1 < 0) {
                simulate_key(KeyEvent.VK_LEFT);

            } else if (c1 > 0) {
                simulate_key(KeyEvent.VK_RIGHT);
            }

            if (c2 < 0) {
                simulate_key(KeyEvent.VK_UP);
            } else if (c2 > 0) {
                simulate_key(KeyEvent.VK_DOWN);
            }

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
    private void parse_device_for_mouse(int c1, int c2, int c3, int c4, int c5, int c6) {
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

        if (c3 == 1) {
            mouse_speed--;
            g.update_speed(mouse_speed);
            return;
        } else if (c3 == -1) {
            mouse_speed++;
            g.update_speed(mouse_speed);
            return;
        } else if (c5 == 1) {
            //leftClickDown(true);
            // delay(30);
            // leftClickDown(false);
//            hw_mouse.send_new("l\n");
            String send_str = "l\n";
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
            return;

        } else if (c6 == 1) {
//            hw_mouse.send_new("r\n");
            String send_str = "r\n";
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
            return;
        }
        System.out.println(mouse.x + "," + mouse.y + "," + screen.width + "," + screen.height);
        if (mouse.x < 5 || mouse.y < 5) {
            System.out.println("haptic and buzzer"+first_boundary_hit);
            if (first_boundary_hit) {
                haptic();
                beep();
                first_boundary_hit = false;
            }
        } else if (mouse.x + 5 > screen.width || mouse.y + 5 > screen.height) {
            System.out.println("haptic and buzzer"+first_boundary_hit);
            if (first_boundary_hit) {
                haptic();
                beep();
                first_boundary_hit = false;
            }
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
//        CtrlKeyChecker.initialize();
        new WheelerDriver();
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

        String[] info = line.trim().split(",");
        int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0, c7 = 0;
        if (info.length == 8) {
            c1 = Integer.parseInt(info[1]);
            c2 = Integer.parseInt(info[2]);
            c3 = Integer.parseInt(info[3]);
            c4 = Integer.parseInt(info[4]);
            c5 = Integer.parseInt(info[5]);
            c6 = Integer.parseInt(info[6]);
            c7 = Integer.parseInt(info[7]);

            c1t += c1;
            c2t += c2;
            c3t += c3;

            if (Math.abs(c1t) == 2) {
                wh += c1;
                c1t = 0;
            }
            if (Math.abs(c2t) == 2) {
                wh2 += c2;
                c2t = 0;
            }
            if (Math.abs(c3t) == 2) {
                wh3 += c3;
                c3t = 0;
            }
            String recreate = "enc," + wh + "," + wh2 + "," + wh3 + "," + c4 + "," + c5 + "," + c6;
//            line = recreate;
            System.out.println("found_recreated=" + line);
        }
        on_device_event(line, c1, c2, c3, c4, c5, c6, c7);
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
