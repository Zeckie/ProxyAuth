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
    /**
     * Note, this method blocks until the action is complete.
     *
     * @param proxyRequest the request to be actioned
     * @return the action succeeded
     * @throws IOException
     */
    boolean action(ProxyRequest proxyRequest) throws IOException;
}
