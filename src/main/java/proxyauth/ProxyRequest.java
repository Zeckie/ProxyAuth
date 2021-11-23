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

import proxyauth.actions.ForwardAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static proxyauth.Utils.ASCII;

/**
 * Handles a single request
 *
 * @author Zeckie
 */
public class ProxyRequest extends Thread {
    public final Socket incomingSocket;
    public final ProxyListener parent;

    /**
     * http headers received, including the request line
     * Note that for CONNECT requests (e.g. for https connections), this will contain the
     * target hostname and port, but not much else.
     */
    public List<String> requestHeaders;

    /**
     * http response headers received from upstream proxy, including the response line
     * Note that for CONNECT requests (e.g. for https connections), this will just be headers
     * from the proxy, not the target server
     */
    public List<String> responseHeaders;

    /**
     * Timestamp when this request started (when the incoming connection was accepted)
     */
    public Date started = new Date();

    /**
     * Counter to give threads unique names
     */
    private static final AtomicLong THREAD_COUNTER = new AtomicLong();

    public ProxyRequest(Socket sock, ProxyListener proxyListener, ThreadGroup threads) {
        super(threads, "ProxyRequest-" + THREAD_COUNTER.incrementAndGet());
        this.incomingSocket = sock;
        this.parent = proxyListener;
    }

    @Override
    public void run() {
        boolean success = false;
        try (incomingSocket) {
            System.out.println("Accepted connection from: " + incomingSocket.getInetAddress() + " port " + incomingSocket.getPort());
            incomingSocket.setSoTimeout(parent.config.SOCKET_TIMEOUT.getValue());
            requestHeaders = processHeaders(incomingSocket.getInputStream());

            success = new ForwardAction(
                    InetAddress.getByName(parent.config.UPSTREAM_PROXY_HOST.getValue()),
                    parent.config.UPSTREAM_PROXY_PORT.getValue(),
                    parent.config.USERNAME.getValue(), parent.config.PASSWORD.getValue()
            ).action(this);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            parent.finished(this, success);
        }
    }

    public List<String> processHeaders(InputStream inputStream) throws IOException {
        final int bufferSize = parent.config.BUF_SIZE.getValue();
        byte[] buf = new byte[bufferSize];
        int bytes_read = 0;

        // read http request headers (ends with 2x CRLF)
        final byte[] end_headers = {'\r', '\n', '\r', '\n'};
        while (bytes_read < 4 || !Arrays.equals(buf, bytes_read - 4, bytes_read, end_headers, 0, 4)) {
            int byte_read = inputStream.read();
            if (byte_read == -1) throw new IOException("End of stream reached before http request headers read");
            if (bytes_read == bufferSize)
                throw new IOException("Buffer full before http request headers read");
            buf[bytes_read++] = (byte) byte_read;
        }

        if (parent.config.DEBUG.getValue()) {
            System.out.println("--- Headers ---");
            System.out.write(buf, 0, bytes_read);
            System.out.println("--- End: Headers ---");
            System.out.flush();
        }

        return Arrays.asList(new String(buf, 0, bytes_read, ASCII).split("\r\n"));
    }
}