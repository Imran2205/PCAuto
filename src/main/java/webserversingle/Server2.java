/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserversingle;

public class Server2 implements EmuInterface {

    public Server2() {
        try {
            EmuServer emu = new EmuServer(this, 80);
            emu.start();
        } catch (Exception ex) {
            System.out.println("Emulator server can't start");
        }
    }

    public static void main(String[] args) throws Exception {
        new Server2();
    }

    @Override
    public void on_emu_data(String data) {
        System.out.println("emu=" + data);
    }
}
