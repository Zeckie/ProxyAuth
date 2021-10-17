package proxyauth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

import static proxyauth.Utils.ascii;

/**
 * Transfers all bytes from input to output, flushing as required to keep things moving
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class PassThrough extends Thread {
    public static final AtomicLong THREAD_COUNTER = new AtomicLong(0);

    InputStream is;
    OutputStream os;
    StatusListener<PassThrough> listener;
    public AtomicLong bytesTransferred = new AtomicLong(0);
    public final String[] headers;

    public PassThrough(StatusListener<PassThrough> listener, InputStream is, OutputStream os, boolean isUp, String[] headers) {
        super("PassThrough-" + THREAD_COUNTER.incrementAndGet() + (isUp ? "-up" : "-down"));
        this.is = is;
        this.os = os;
        this.listener = listener;
        this.headers = headers;
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
                        is.close();
                        os.close();
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
                if (Configuration.DEBUG) se.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.finished(this, succeeded);
        }
    }
}