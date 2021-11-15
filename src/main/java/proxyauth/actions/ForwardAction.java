package proxyauth.actions;

import proxyauth.ProxyRequest;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Forwards the request to another proxy server
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class ForwardAction implements Action {

    final InetAddress host;
    final int port;
    final String username;
    final String password;

    public ForwardAction(InetAddress host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean action(ProxyRequest proxyRequest) throws IOException {
        ForwardRequest req = new ForwardRequest(proxyRequest, this);
        return req.go();
    }
}