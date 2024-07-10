package pcauto;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import myarduinolib.ArduinoCmdClient;
import myarduinolib.SerialNR;
import simplenet.nsojib.github.io.*;
import websocketj.WebSocketServerSingle;

/**
 *
 * @author FridayLab
 */
public class PCAuto_b4_websocket_backup implements SimpleNetServerListener, websocketj.WebsocketInterface {

    GUI g;
    Robot robot;
    static final String TAG = ArduinoCmdClient.class.getSimpleName();
    private String port = "COM6";
//    private int baud = 230400;
    private int baud = 9600;
    private SerialNR ms;
    private boolean isReady = false;
    SimpleNetServer server;
    int simple_net_port = 4555;

    int wh = 0, wh2 = 0, wh3 = 0;

    int c1t = 0, c2t = 0, c3t = 0;

    WebSocketServerSingle ws;

    public PCAuto_b4_websocket_backup() {
//        g = new GUI(this);
        g.setVisible(true);
        g.setLocationRelativeTo(null);
        try {
            robot = new Robot();
        } catch (Exception ex) {
            System.out.println("Error=" + ex.getMessage());
        }

//        new Thread() {
//            public void run() {
//                for (int i = 0; i < 100; i++) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (Exception ex) {
//
//                    }
//                    robot.keyPress(KeyEvent.VK_UP);
//                }
//            }
//        }.start();
        try {
            System.out.println("Connecting with arduino_cmd_server on port " + port + " at " + baud + " rate");
            ms = new SerialNR(port, baud);
//            ms.disDebug();
            ms.enDebug();
            System.out.println("Connected");

//            System.out.println("Waiting for ready.");
//            checkReady();
            g.setTitle(g.getTitle() + "_Connected");
        } catch (Exception ex) {
            g.setTitle(g.getTitle() + "_Not Connected");
            System.out.println("Can't connect");
            ex.printStackTrace();
        }

        new Thread() {
            public void run() {
                System.out.println("Device Reading started...");
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {

                    }
                    //enc,-1,0,0,0,0,0 //v2 data
                    String line = ms.readLine2();
                    System.out.println("found=" + line);
                    String[] info = line.trim().split(",");
                    if (info.length == 7) {
                        int c1 = Integer.parseInt(info[1]);
                        int c2 = Integer.parseInt(info[2]);
                        int c3 = Integer.parseInt(info[3]);
                        int c4 = Integer.parseInt(info[4]);
                        int c5 = Integer.parseInt(info[5]);
                        int c6 = Integer.parseInt(info[6]);

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
                        line = recreate;
                        System.out.println("found_recreated=" + line);

                    }
                    //older version
                    onDeviceEvent(line);
                    info = line.trim().split(",");
                    if (info.length == 7) {
                        String back = info[6];
                        int back_b = Integer.parseInt(back);
                        if (back_b == 1) {
                            //generate escape key
                            System.out.println("Simulating escape key");
                            robot.keyPress(KeyEvent.VK_ESCAPE);
                        }
                    }

//                    onChange(line);
                }
            }
        }.start();

        System.out.println("starting websocket server on port=" + 8000);
        try {
            ws = new WebSocketServerSingle(this, 8000);
            ws.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Starting socket server");
        try {
            server = new SimpleNetServer(0, simple_net_port, this);
            server.runAllDayLong();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void onDeviceEvent(String data) {
        System.out.println("onDeviceEvent=" + data);
        if (server != null) {
            server.send(data);
        }

        if (ws.isConnected()) {
            ws.send_msg(data);
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
        Vector<String> lines = ms.readResponse();
        for (String line : lines) {
            if (line.toLowerCase().contains("ready")) {
                isReady = true;
                System.out.println("IS Ready");
                System.out.println("isready");
                return;
            }
        }
    }

    public void onButton(String name) {
        System.out.println("name=" + name);
        if (server != null) {
            server.send("gui=" + name);
        }

        ms.enDebug();
        if (name.equals("up")) {
            beep();
        } else if (name.equals("down")) {
            haptic();
        }
//        } 
//        if (name.equals("up")) {
//            robot.keyPress(KeyEvent.VK_UP);
//        } else if (name.equals("down")) {
//            robot.keyPress(KeyEvent.VK_DOWN);
//        }
    }

    public void beep() {
        boolean s = ms.send_new("b\n");
        System.out.println("s=" + s);
    }

    public void haptic() {
        boolean s = ms.send_new("h\n");
        System.out.println("s=" + s);
    }

    public static void main(String[] args) {
        new PCAuto_b4_websocket_backup();
    }

    @Override
    public void newSimpleMsg(int serverId, String line) {
        System.out.println("newmsg=" + line);
        if (line.equals("beep")) {
            beep();
        } else if (line.equals("haptic")) {
            haptic();
        }
    }

    @Override
    public void onSimpleDisconnect(int serverId) {
        System.out.println("disconnected=" + serverId);
    }

    @Override
    public void newSimpleClient(int serverId, String ip) {
        System.out.println("connected=" + ip);
    }

    @Override
    public void onopen(String remote) {
        System.out.println("websocket:onopen=" + remote);
    }

    @Override
    public void onmessage(String msg) {
        System.out.println("websocket:onmessage=" + msg);
    }

    @Override
    public void onclose() {
        System.out.println("websocket:connection closed");
    }

}
