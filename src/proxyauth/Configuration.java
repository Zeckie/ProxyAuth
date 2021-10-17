package proxyauth;

import proxyauth.actions.Action;
import proxyauth.actions.ForwardAction;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Configuration is currently hard coded
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Configuration {
    private Configuration() {
    }

    public static final int BUF_SIZE = 10240;
    public static final boolean DEBUG = true;

    public static final int LISTEN_BACKLOG = 5;
    public static final int LISTEN_PORT = 8080;
    public static Action INITIAL_ACTION;


    static InetAddress listenAddress = null;
    public static final int MAX_ACTIVE_REQUESTS = 20;

    static void init() throws IOException {
        listenAddress = InetAddress.getByName("127.0.0.127");
        INITIAL_ACTION = new ForwardAction(InetAddress.getByName("proxy.example.com"), 8080);
    }
}
