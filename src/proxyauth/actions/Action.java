package proxyauth.actions;

import proxyauth.ProxyRequest;

import java.io.IOException;

/**
 * Performs an action on a proxy request - eg. forward it
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public interface Action {
    void action(ProxyRequest proxyRequest) throws IOException;
}
