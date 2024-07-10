/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserversingle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 *
 * @author FridayLab
 */
public class EmuServer {

    HttpServer server;
    EmuInterface listener;
    public EmuServer(EmuInterface listener, int port) throws Exception {
        this.listener=listener;
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void start() {
        server.createContext("/", new HandlerIndex());
        server.createContext("/emu", new HandlerEmu());
        server.start();
        System.out.println("server started");
    }

    class HandlerIndex implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            String fn = "index.html";
            File file = new File(fn);

            //fileId=/hi/enc,3,4,2
            String fileId = exchange.getRequestURI().getPath();
            System.out.println("fileId=" + fileId);
            if (!fileId.equals("/")) {
                file = new File(fileId);
            }
            if (!file.exists()) {
                String resp = "File " + fileId + " not found";
                exchange.sendResponseHeaders(200, resp.length());
                OutputStream os = exchange.getResponseBody();

                os.write(resp.getBytes());
                os.close();
                return;
            }

            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
            fs.close();
        }
    }

    class HandlerEmu implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            String fileId = exchange.getRequestURI().getPath().replace("/emu/", "");
//            System.out.println("emudata=" + fileId);
            listener.on_emu_data(fileId);
            
            String resp = "success";
            exchange.sendResponseHeaders(200, resp.length());
            OutputStream os = exchange.getResponseBody();

            os.write(resp.getBytes());
            os.close();
        }
    }
}
