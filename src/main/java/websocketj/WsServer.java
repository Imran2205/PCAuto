/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package websocketj;

;

/**
 *
 * @author FridayLab
 */

/*
from scratch
 */
public class WsServer implements WebsocketInterface {

    public WsServer() {
        WebSocketServerSingle ws = new WebSocketServerSingle(this, 8000);
        ws.start();
        boolean s = ws.send_msg("not_send");
        System.out.println("s=" + s);
    }

    public static void main(String[] args) {
        WsServer ws = new WsServer();
    }

    @Override
    public void onopen(String remote) {
        System.out.println("onopen=" + remote);
    }

    @Override
    public void onmessage(String msg) {
        System.out.println("onmessage=" + msg);
    }

    @Override
    public void onclose() {
        System.out.println("connection closed");
    }

}
