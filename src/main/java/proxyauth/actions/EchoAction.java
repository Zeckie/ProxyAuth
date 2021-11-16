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

import proxyauth.ProxyRequest;
import proxyauth.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Treat the incoming request as a HTTP get, and reply by echoing the headers
 *
 * @author Zeckie
 */
public class EchoAction implements Action {

    @Override
    public boolean action(ProxyRequest proxyRequest) throws IOException {
        boolean success;
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(proxyRequest.incomingSocket.getOutputStream(), Utils.ASCII), false);
        String requestLine = proxyRequest.requestHeaders.get(0);
        if (requestLine.startsWith("GET ")) {
            pw.println("HTTP/1.1 200 Echoing your request\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "Received request + headers:"
            );
            success = true;
        } else {
            pw.println("HTTP/1.1 501 Not Implemented\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Connection: close\r\n\r\n"
            );
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
