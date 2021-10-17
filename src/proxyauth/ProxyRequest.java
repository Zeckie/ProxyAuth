package proxyauth;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static proxyauth.Configuration.DEBUG;
import static proxyauth.Utils.ASCII;
/**
 * Handles a single request
 * @author Zeckie
 * Copyright and licence details in Main.java
*/
public class ProxyRequest extends Thread {
    public final Socket incomingSocket;
    final ProxyListener parent;

    /** headers includes request line */
    public String[] headers;

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
        System.out.println("Accepted connection from: " + incomingSocket.getInetAddress() + " port " + incomingSocket.getPort());
        boolean success = false;
        try (InputStream inputStream = incomingSocket.getInputStream()) {

            processHeaders(inputStream);

            success = Configuration.INITIAL_ACTION.action(this);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            parent.finished(this, success);
        }
    }

    private void processHeaders(InputStream inputStream) throws IOException {
        byte[] buf = new byte[Configuration.BUF_SIZE];
        int bytes_read = 0;

        // read http request headers (ends with 2x CRLF)
        final byte[] end_headers = {'\r', '\n', '\r', '\n'};
        while (bytes_read < 4 || !Arrays.equals(buf, bytes_read - 4, bytes_read, end_headers, 0, 4)) {
            int byte_read = inputStream.read();
            if (byte_read == -1) throw new IOException("End of stream reached before http request headers read");
            if (bytes_read == Configuration.BUF_SIZE)
                throw new IOException("Buffer full before http request headers read");
            buf[bytes_read++] = (byte) byte_read;
        }

        if (DEBUG) {
            System.out.println("--- Headers ---");
            System.out.write(buf, 0, bytes_read);
            System.out.println("--- End: Headers ---");
            System.out.flush();
        }

        headers = new String(buf, 0, bytes_read, ASCII).split("\r\n");
    }
}