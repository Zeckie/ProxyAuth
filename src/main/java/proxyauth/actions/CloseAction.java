package proxyauth.actions;

import proxyauth.ProxyRequest;

import java.io.IOException;
/**
 * Simple action - just close the connection
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class CloseAction implements Action {

    @Override
    public boolean action(ProxyRequest proxyRequest) throws IOException {
        proxyRequest.incomingSocket.close();
        return true;
    }
}
