/*
 * This file is part of ProxyAuth - https://github.com/Zeckie/ProxyAuth
 * ProxyAuth is Copyright (c) 2022 Zeckie and contributors
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
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Check the password using the Pwned Passwords API from Have I Been Pwned
 * (<a href="https://haveibeenpwned.com/API/v3#PwnedPasswords">https://haveibeenpwned.com/API/v3#PwnedPasswords</a>)
 * </p>
 * <p>
 * Uses https to protect against the request being intercepted, and hash based
 * k-Anonymity so even haveibeenpwned.com cannot be sure which password being checked(
 * <a href="https://www.troyhunt.com/ive-just-launched-pwned-passwords-version-2/#cloudflareprivacyandkanonymity">
 * https://www.troyhunt.com/ive-just-launched-pwned-passwords-version-2/#cloudflareprivacyandkanonymity</a>
 * </p>
 */
public class HIBPCheck implements Runnable {

    private final Configuration config;

    public HIBPCheck(Configuration config) {
        this.config = config;
    }

    /** Run HIBP check in a new thread */
    public void go() {
        new Thread(this).start();
    }

    /** Run HIBP check in the current thread */
    public void run() {

        try {
            System.out.println("Checking password ...");
            int count = check();
            if (count==0) {
                System.out.println("Password check ok");
            } else {
                System.err.println("Password found in "+count+" breaches - HIBP");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Caught exception: "+e.getMessage(),e);
        }

    }

    private static final String API_URL = "https://api.pwnedpasswords.com/range/";


    private int check() throws IOException, NoSuchAlgorithmException {

        String hash = SHA1.hash(config.PASSWORD.getValue());
        String url = API_URL + hash.substring(0, 5);

        URL u = new URL(url);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.LISTEN_ADDRESS.getValue(), config.LISTEN_PORT.getValue()));
        HttpURLConnection con = (HttpURLConnection) u.openConnection(proxy);
        con.setRequestMethod("GET");
        con.addRequestProperty("Add-Padding", "true");
        con.connect();
        if (con.getResponseCode() == 200) {
            String s = new String(con.getInputStream().readAllBytes()).toLowerCase();
            String[] lines = s.split("\r\n");
            for (String line : lines) {
                if (line.substring(0, 35).equals(hash.substring(5))) {
                    String count = line.substring(36);
                    return Integer.parseInt(count);
                }
            }
        } else {
            throw new IOException("Unexpected http response: " + con.getResponseCode() + " " + con.getResponseMessage());
        }
        return 0;
    }
}
