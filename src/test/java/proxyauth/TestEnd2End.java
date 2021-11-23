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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import proxyauth.conf.Configuration;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests multiple components, through use of mock proxy server
 */
public class TestEnd2End {


    public static final Charset ASCII = StandardCharsets.US_ASCII;

    public void doE2ETest(boolean close, final String request, final String expectedRequest, final String response, final String expectedResponse) throws IOException, InterruptedException {
        /* Use mostly default configuration, override values important to test */
        Configuration dummy = new Configuration();
        dummy.UPSTREAM_PROXY_HOST.setValue("127.0.1.1");
        dummy.LISTEN_ADDRESS.setValue("127.0.1.2");
        dummy.LISTEN_PORT.setValue(0); // Ephemeral port
        dummy.USERNAME.setValue("foo");
        dummy.PASSWORD.setValue("bar");
        dummy.CONNECTION_CLOSE.setValue(close);

        List<Closeable> toClose = new ArrayList<>();
        try {
            // Start mock proxy listening
            ServerSocket serverSocket = new ServerSocket(
                    0, 1,
                    InetAddress.getByName(dummy.UPSTREAM_PROXY_HOST.getValue())
            );
            dummy.UPSTREAM_PROXY_PORT.setValue(serverSocket.getLocalPort());
            toClose.add(serverSocket);


            // Start ProxyListener
            ProxyListener listener = new ProxyListener(dummy);
            toClose.add(listener);
            Thread proxyThread = new Thread(listener);
            proxyThread.start();

            while (listener.getLocalPort() == null) Thread.sleep(100);

            // Connect to ProxyListener
            Socket clientSocket = new Socket(
                    dummy.LISTEN_ADDRESS.getValue(), listener.getLocalPort()
            );
            toClose.add(clientSocket);
            final OutputStream clientOutputStream = clientSocket.getOutputStream();
            toClose.add(clientOutputStream);

            // Sent request to ProxyListener
            // TODO: multiple threads to handle blocking, timeouts etc
            clientOutputStream.write(request.getBytes(ASCII));
            clientOutputStream.flush();
            clientSocket.shutdownOutput();

            // Accept connection using mock proxy
            final Socket acceptedSocket = serverSocket.accept();
            toClose.add(acceptedSocket);
            String received = new String(acceptedSocket.getInputStream().readAllBytes(), ASCII);
            Assertions.assertEquals(expectedRequest, received);

            // Send response back
            final OutputStream acceptedSocketOutputStream = acceptedSocket.getOutputStream();
            acceptedSocketOutputStream.write(response.getBytes(ASCII));
            acceptedSocketOutputStream.flush();
            acceptedSocket.shutdownOutput();

            // Verify response
            received = new String(clientSocket.getInputStream().readAllBytes(), ASCII);
            Assertions.assertEquals(expectedResponse, received);
            
        } finally {
            System.err.println("Cleanup");
            for (Closeable c : toClose) {
                c.close();
            }
        }
    }

    @Timeout(10000L)
    @Test
    public void withClose() throws IOException, InterruptedException {
        doE2ETest(
                true,
                "FOO http://bar/ HTTP/1.1\r\nBaz: 1\r\n\r\n",
                "FOO http://bar/ HTTP/1.1\r\n" +
                        "Baz: 1\r\n" +
                        "Proxy-Authorization: Basic Zm9vOmJhcg==\r\n" +
                        "Connection: Close\r\n\r\n",
                "HTTP/1.1 123 Foo\r\nBar\r\n\r\n",
                "HTTP/1.1 123 Foo\r\nBar\r\nConnection: Close\r\n\r\n"
        );
    }

    @Timeout(10000L)
    @Test
    public void withoutClose() throws IOException, InterruptedException {
        doE2ETest(
                false,
                "FOO http://bar/ HTTP/1.1\r\nBaz: 1\r\n\r\n",
                "FOO http://bar/ HTTP/1.1\r\n" +
                        "Baz: 1\r\n" +
                        "Proxy-Authorization: Basic Zm9vOmJhcg==\r\n\r\n",
                "HTTP/1.1 123 Foo\r\nBar\r\n\r\n",
                "HTTP/1.1 123 Foo\r\nBar\r\n\r\n"
        );
    }

}
