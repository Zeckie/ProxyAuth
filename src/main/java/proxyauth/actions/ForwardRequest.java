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

package proxyauth.actions;

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

import static proxyauth.Utils.ASCII;
import static proxyauth.Utils.ascii;


/**
 * Forwards a request to a proxy server
 *
 * @author Zeckie
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
     * @param headers HTTP request headers
     * @return a copy of headers, with the configured proxy authorization
     */
    public List<String> processAuthHeaders(List<String> headers) {
        headers = new ArrayList<String>(headers);
        headers.removeIf(s -> s.toLowerCase().startsWith("proxy-authorization:"));
        headers.add("Proxy-Authorization: Basic " + new String(Base64.getEncoder().encode(ascii(action.username + ":" + action.password)), ASCII));
        return headers;
    }

    /**
     * @param headers
     * @return a copy of headers, modified to stop keep-alive
     */
    public List<String> processKeepAlive(List<String> headers) {
        headers = new ArrayList<String>(headers);
        headers.removeIf(s -> s.toLowerCase().startsWith("connection:"));
        headers.removeIf(s -> s.toLowerCase().startsWith("keep-alive:"));
        headers.add("Connection: Close");
        return headers;
    }

    public boolean go() throws IOException {
        PassThrough upload;
        PassThrough download;

        try (Socket upstream = new Socket()) {
            upstream.setSoTimeout(proxyRequest.parent.config.SOCKET_TIMEOUT.getValue());
            upstream.connect(new InetSocketAddress(action.host, action.port), proxyRequest.parent.config.SOCKET_TIMEOUT.getValue());
            this.upstreamSocket = upstream;
            BufferedOutputStream outputStream = new BufferedOutputStream(upstream.getOutputStream(), proxyRequest.parent.config.BUF_SIZE.getValue());

            if (proxyRequest.parent.config.DEBUG.getValue()) System.out.println("upstream socket = " + upstream);

            List<String> headers = processAuthHeaders(proxyRequest.requestHeaders);
            if (proxyRequest.parent.config.CONNECTION_CLOSE.getValue()) {
                headers = processKeepAlive(headers);
            }
            upload = new PassThrough(this, proxyRequest.incomingSocket.getInputStream(), outputStream, upstream,
                    true, headers, proxyRequest.parent.config);
            upload.start();

            proxyRequest.responseHeaders = proxyRequest.processHeaders(upstream.getInputStream());

            if (proxyRequest.parent.config.STOP_ON_PROXY_AUTH_ERROR.getValue()) {
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

            List<String> respHeaders = proxyRequest.responseHeaders;
            if (proxyRequest.parent.config.CONNECTION_CLOSE.getValue()) {
                respHeaders = processKeepAlive(respHeaders);
            }

            download = new PassThrough(
                    this, upstream.getInputStream(),
                    new BufferedOutputStream(
                            proxyRequest.incomingSocket.getOutputStream(), proxyRequest.parent.config.BUF_SIZE.getValue()
                    ),
                    proxyRequest.incomingSocket, false, respHeaders, proxyRequest.parent.config
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
            if (proxyRequest.parent.config.DEBUG.getValue())
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
