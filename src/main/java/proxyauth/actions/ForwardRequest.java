package proxyauth.actions;

import proxyauth.Configuration;
import proxyauth.PassThrough;
import proxyauth.ProxyRequest;
import proxyauth.StatusListener;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import static proxyauth.Configuration.SOCKET_TIMEOUT;
import static proxyauth.Utils.ASCII;
import static proxyauth.Utils.ascii;

/**
 * Forwards a request to a proxy server
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class ForwardRequest implements StatusListener<PassThrough> {
    static final Pattern PROXY_AUTH_ERROR = Pattern.compile("^HTTP/\\d.\\d 407 .*");

    private final ForwardAction action;
    /**
     * The request being forwarded
     */
    private final ProxyRequest proxyRequest;
    /**
     * Connection to upstream proxy server
     */
    private Socket upstreamSocket;
    private volatile boolean anyErrors = false;

    public ForwardRequest(ProxyRequest proxyRequest, ForwardAction forwardAction) {
        this.proxyRequest = proxyRequest;
        this.action = forwardAction;
    }

    /**
     * Return a copy of headers, with the configured proxy authorization
     *
     * @return
     */
    public List<String> processAuthHeaders(List<String> headers) {
        headers = new ArrayList<String>(headers);
        headers.removeIf(s -> s.toLowerCase().startsWith("proxy-authorization:"));
        headers.add("Proxy-Authorization: Basic " + new String(Base64.getEncoder().encode(ascii(action.username + ":" + action.password)), ASCII));
        return headers;
    }

    public boolean go() throws IOException {
        PassThrough upload;
        PassThrough download;

        try (Socket upstream = new Socket()) {
            upstream.setSoTimeout(SOCKET_TIMEOUT);
            upstream.connect(new InetSocketAddress(action.host, action.port), SOCKET_TIMEOUT);
            this.upstreamSocket = upstream;
            BufferedOutputStream outputStream = new BufferedOutputStream(upstream.getOutputStream(), Configuration.BUF_SIZE);

            if (Configuration.DEBUG) System.out.println("upstream socket = " + upstream);

            upload = new PassThrough(this, proxyRequest.incomingSocket.getInputStream(), outputStream, true, processAuthHeaders(proxyRequest.requestHeaders));
            upload.start();

            proxyRequest.responseHeaders = ProxyRequest.processHeaders(upstream.getInputStream());

            if (Configuration.STOP_ON_PROXY_AUTH_ERROR) {
                final String line = proxyRequest.responseHeaders.get(0);
                if (PROXY_AUTH_ERROR.matcher(line).matches()) {
                    System.err.println("STOPPING due to proxy auth error: " + line);
                    System.exit(5); //magic number 5 often = access denied
                    /*
                     * TODO: change to respond to all requests with error page,
                     * instead of quitting
                     */
                }
            }

            download = new PassThrough(
                    this, upstream.getInputStream(),
                    new BufferedOutputStream(proxyRequest.incomingSocket.getOutputStream(), Configuration.BUF_SIZE),
                    false,
                    proxyRequest.responseHeaders
            );

            download.start();
            try {
                // Wait for streams to be closed
                upload.join();
                download.join();
            } catch (
                    InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (this) {
            System.out.println(Thread.currentThread() + " Finished");
            if (Configuration.DEBUG)
                System.out.println(
                        "--Finished--\n"
                                + " - any errors: " + anyErrors + "\n"
                                + " - request: " + proxyRequest.requestHeaders.get(0) + "\n"
                                + " - upload: " + upload.bytesTransferred.get() + "\n"
                                + " - download: " + download.bytesTransferred.get() + "\n"
                                + " - elapsed: " + (System.currentTimeMillis() - proxyRequest.started.getTime())
                );
            return !anyErrors;
        }

    }

    @Override
    public synchronized void finished(PassThrough obj, boolean succeeded) {
        if (!succeeded) {
            this.anyErrors = true;

            // Close both sockets
            if (upstreamSocket != null) {
                try {
                    upstreamSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                proxyRequest.incomingSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
