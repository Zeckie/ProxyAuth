package proxyauth;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listen for requests and create threads to handle them
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class ProxyListener implements Runnable {

    @Override
    public void run() {
        try (ServerSocket incoming = new ServerSocket(Configuration.LISTEN_PORT, Configuration.LISTEN_BACKLOG, Configuration.listenAddress)) {
            System.out.println("Listening " + incoming);

            // initially quit after a few requests, change this when we can process them
            for (int i = 0; i < 10; i++) {
                Socket sock = incoming.accept();
                new ProxyRequest(sock, this).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
