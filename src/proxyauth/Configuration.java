package proxyauth;

import proxyauth.actions.Action;
import proxyauth.actions.EchoAction;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Configuration is currently hard coded
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Configuration {
    private Configuration() {}
    static final int BUF_SIZE = 10240;
    static final boolean DEBUG=true;

    static final int LISTEN_BACKLOG = 5;
    static final int LISTEN_PORT = 8080;
    static final Action INITIAL_ACTION=new EchoAction();
    static InetAddress listenAddress = null;

    static void init() throws IOException {
        listenAddress = InetAddress.getByName("127.0.0.127");
    }
}
