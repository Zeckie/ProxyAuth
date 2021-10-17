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
public record ForwardAction(InetAddress host, int port) implements Action {

    public ForwardAction(InetAddress host, int port) {
        this.host = host;
        this.port = port;
        assert (port > 0);
    }

    @Override
    public boolean action(ProxyRequest proxyRequest) throws IOException {
        ForwardRequest req = new ForwardRequest(proxyRequest, this);
        return req.go();
    }
}