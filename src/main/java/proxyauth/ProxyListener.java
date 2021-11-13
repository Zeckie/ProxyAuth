package proxyauth;

import proxyauth.conf.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Listen for requests and create threads to handle them
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class ProxyListener implements Runnable, StatusListener<ProxyRequest> {

    /**
     * ThreadGroup containing threads that we start
     */
    private static final ThreadGroup THREADS = new ThreadGroup("proxyauth");

    /**
     * requests that have been accepted but not finished
     */
    private final Set<ProxyRequest> activeRequests = new LinkedHashSet<>();


    public final Configuration config;

    public ProxyListener(Configuration configuration) {
        config = configuration;
    }


    @Override
    public void run() {

        try (
                ServerSocket incoming = new ServerSocket(
                        config.LISTEN_PORT.getValue(),
                        config.LISTEN_BACKLOG.getValue(),
                        InetAddress.getByName(config.LISTEN_ADDRESS.getValue())
                )
        ) {
            System.out.println("Listening " + incoming);

            //noinspection InfiniteLoopStatement (CTRL+C to stop)
            while (true) {
                Socket sock = incoming.accept();

                ProxyRequest proxyRequest = new ProxyRequest(sock, this, THREADS);
                synchronized (activeRequests) {
                    activeRequests.add(proxyRequest);
                    activeRequests.notifyAll();
                    proxyRequest.start();
                    while (activeRequests.size() >= config.MAX_ACTIVE_REQUESTS.getValue()) {
                        System.out.println("Active request limit reached - waiting for a request to finish");
                        activeRequests.wait();
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finished(ProxyRequest obj, boolean succeeded) {
        synchronized (activeRequests) {
            activeRequests.remove(obj);

            System.out.println("Finished " + obj + " success=" + succeeded + "\n" +
                    "Active requests:" + activeRequests.size() + "\n" +
                    "Active threads:" + THREADS.activeCount()
            );

            if (config.DEBUG.getValue()) {
                THREADS.list();
            }

            activeRequests.notifyAll();
        }
    }
}
