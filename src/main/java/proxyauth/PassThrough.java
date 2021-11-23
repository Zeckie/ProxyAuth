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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static proxyauth.Utils.ascii;

/**
 * Transfers all bytes from input to output, flushing as required to keep things moving
 *
 * @author Zeckie
 */
public class PassThrough extends Thread {
    public static final AtomicLong THREAD_COUNTER = new AtomicLong(0);
    private final Socket toShutdownOutput;

    InputStream is;
    OutputStream os;
    StatusListener<PassThrough> listener;
    public AtomicLong bytesTransferred = new AtomicLong(0);
    public final List<String> headers;
    final Configuration config;

    /**
     * Write the supplied http headers, then copy all bytes from input to output
     *
     * @param listener         Listener to notify when finished
     * @param is               stream to read bytes from
     * @param os               stream to write bytes to
     * @param toShutdownOutput (Optional) socket to shut down output of when done
     * @param isUp             direction (is this uploading?)
     * @param headers          list of http headers
     * @param config           configuration
     */
    public PassThrough(StatusListener<PassThrough> listener, InputStream is, OutputStream os, Socket toShutdownOutput,
                       boolean isUp, List<String> headers, Configuration config) {
        super("PassThrough-" + THREAD_COUNTER.incrementAndGet() + (isUp ? "-up" : "-down"));
        this.is = is;
        this.os = os;
        this.toShutdownOutput = toShutdownOutput;
        this.listener = listener;
        this.headers = headers;
        this.config = config;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread() + " Started");
        boolean succeeded = true;
        try {
            try {

                if (headers != null) {
                    // Send the headers, followed by blank line
                    for (String header : headers) {
                        os.write(ascii(header + "\r\n"));
                    }
                    os.write(ascii("\r\n"));
                }

                // transfer remaining bytes (eg. body)
                while (true) {
                    if (is.available() == 0) os.flush();
                    int nxt = is.read();
                    if (nxt == -1) {
                        if (toShutdownOutput!=null) toShutdownOutput.shutdownOutput();
                        System.out.println(Thread.currentThread() + " Finished. Bytes=" + bytesTransferred.get());
                        return;
                    }
                    bytesTransferred.incrementAndGet();
                    os.write(nxt);
                }
            } catch (SocketException se) {
                /* Fairly common - e.g. when either side closes the connection with TCP reset.
                    However, we need to make sure we clean up any resources, such as other sockets.
                 */
                succeeded = false;
                os.close();
                is.close();
                System.out.println(Thread.currentThread() + " SocketException -> closed. Bytes=" + bytesTransferred.get());
                if (config.DEBUG.getValue()) se.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.finished(this, succeeded);
        }
    }
}