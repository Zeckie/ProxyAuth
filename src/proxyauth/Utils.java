package proxyauth;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Utils {
    public static final Charset ASCII = StandardCharsets.US_ASCII;

    /**
     * Convert String to raw ascii bytes
     */
    public static byte[] ascii(String in) {
        return in.getBytes(ASCII);
    }
}