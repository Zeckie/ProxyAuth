/*
 * This file is part of ProxyAuth - https://github.com/Zeckie/ProxyAuth
 * ProxyAuth is Copyright (c) 2021 Zeckie
 *
 * ProxyAuth is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * ProxyAuth is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with ProxyAuth. If you have the source code, this is in a file called
 * LICENSE. If you have the built jar file, the licence can be viewed by
 * running "java -jar ProxyAuth-<version>.jar licence".
 * Otherwise, see <https://www.gnu.org/licenses/>.
 */

package proxyauth;

import proxyauth.conf.Configuration;
import proxyauth.logging.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listen for requests and create threads to handle them
 *
 * @author Zeckie
 */
public class ProxyListener implements Runnable, StatusListener<ProxyRequest>, Closeable {

    /**
     * ThreadGroup containing threads that we start
     */
    private static final ThreadGroup THREADS = new ThreadGroup("proxyauth");
    private static final Logger LOGGER = Log.logger(ProxyListener.class);

    /**
     * requests that have been accepted but not finished
     */
    private final Set<ProxyRequest> activeRequests = new LinkedHashSet<>();


    public final Configuration config;

    public ProxyListener(Configuration configuration) {
        config = configuration;
    }

    private volatile ServerSocket incoming;

    @Override
    public void run() {

        try (
                ServerSocket incoming = new ServerSocket(
                        config.LISTEN_PORT.getValue(),
                        config.LISTEN_BACKLOG.getValue(),
                        InetAddress.getByName(config.LISTEN_ADDRESS.getValue())
                )
        ) {
            this.incoming = incoming;
            LOGGER.info("Listening " + incoming);

            //noinspection InfiniteLoopStatement (CTRL+C to stop)
            while (true) {
                Socket sock = incoming.accept();

                ProxyRequest proxyRequest = new ProxyRequest(sock, this, THREADS);
                synchronized (activeRequests) {
                    activeRequests.add(proxyRequest);
                    activeRequests.notifyAll();
                    proxyRequest.start();
                    while (activeRequests.size() >= config.MAX_ACTIVE_REQUESTS.getValue()) {
                        LOGGER.warning("Active request limit reached - waiting for a request to finish");
                        activeRequests.wait();
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Caught exception", e);
        }
    }

    @Override
    public void finished(ProxyRequest obj) {
        synchronized (activeRequests) {
            activeRequests.remove(obj);

            LOGGER.fine("Finished " + obj + "\n" +
                    "Active requests:" + activeRequests.size() + "\n" +
                    "Active threads:" + THREADS.activeCount()
            );


            if (LOGGER.isLoggable(Level.FINEST)) {
                Thread[] threads = new Thread[2 * THREADS.activeCount() + 10];
                int count = THREADS.enumerate(threads, true);
                count = Math.min(count, threads.length);
                StringBuilder sb = new StringBuilder();
                sb.append("Active threads (").append(count).append("):");
                for (int i = 0; i < count; i++) {
                    sb.append("\n - " + threads[i]);
                }
                LOGGER.finest(sb.toString());
            }

            activeRequests.notifyAll();
        }
    }

    /**
     * currently used only for testing - close the listening socket
     */
    @Override
    public void close() throws IOException {
        if (incoming != null) incoming.close();
    }

    public Integer getLocalPort() {
        if (incoming != null) return incoming.getLocalPort();
        return null;
    }
}
