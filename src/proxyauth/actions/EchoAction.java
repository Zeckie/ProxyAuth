package proxyauth.actions;

import proxyauth.ProxyRequest;
import proxyauth.Utils;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Treat the incoming request as a HTTP get, and reply by echoing the headers
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class EchoAction implements Action {

    @Override
    public boolean action(ProxyRequest proxyRequest) throws IOException {
        boolean success;
        PrintWriter pw = new PrintWriter(proxyRequest.incomingSocket.getOutputStream(), false, Utils.ASCII);
        String requestLine = proxyRequest.requestHeaders[0];
        if (requestLine.startsWith("GET ")) {
            pw.println("""
                    HTTP/1.1 200 Echoing your request
                    Content-Type: text/plain
                    Connection: close
                            
                    Received request + headers:
                    """);
            success = true;
        } else {
            pw.println("""
                    HTTP/1.1 501 Not Implemented
                    Content-Type: text/plain
                    Connection: close
                                
                    """);
            success = false;
        }

        for (String line : proxyRequest.requestHeaders) {
            pw.println(line);
        }
        pw.flush();
        pw.close();
        return success;
    }
}
