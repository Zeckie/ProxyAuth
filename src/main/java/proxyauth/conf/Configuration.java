package proxyauth.conf;

import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;


/**
 * Manages all user configurable settings for ProxyAuth
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Configuration {

    /* Tuning */
    public final Setting<Integer> BUF_SIZE = new Setting<>(1024, Converter.INTEGER,
            false, "Size of each buffer, in bytes.", null, 100, null);
    public final Setting<Boolean> DEBUG = new Setting<>(true, Converter.YES_NO,
            false, "Print debug details?", null, null, null);
    public final Setting<Integer> SOCKET_TIMEOUT = new Setting<>(180000, Converter.INTEGER,
            false, "Timeout in milliseconds used when connecting, reading and writing to TCP sockets",
            null, 0, null);
    public final Setting<Integer> LISTEN_BACKLOG = new Setting<>(50, Converter.INTEGER,
            false, "Number of incoming connections that can be queued. Setting this too low will result in connections being refused",
            null, 0, null);
    public final Setting<Boolean> STOP_ON_PROXY_AUTH_ERROR = new Setting<>(true, Converter.YES_NO,
            false, "Immediately stop on http error 407, to prevent account from being locked due to multiple attempts with wrong password",
            null, null, null);
    public final Setting<Integer> MAX_ACTIVE_REQUESTS = new Setting<>(20, Converter.INTEGER,
            false, "The number of concurrent requests that can be processed. Higher values will use more resources.", null, 1, null);

    /* Addresses */
    public final Setting<String> LISTEN_ADDRESS = new Setting<>("127.0.0.127", Converter.STRING,
            false, "Local IP address to listen on. Use a loopback address 127.* to make proxy only accessible to processes running locally",
            new Validator() {
                @Override
                public void validate(String val) {
                    try {
                        // Check that the specified address is local
                        final InetAddress addr = InetAddress.getByName(val);
                        new ServerSocket(0, 0, addr);
                    } catch (IOException e) {
                        throw new InvalidSettingException("Not able to listen on specified address - " + e);
                    }
                }
            }, null, null);
    public final Setting<Integer> LISTEN_PORT = new Setting<>(8080, Converter.INTEGER,
            false, "TCP port to listen on. Port 8080 is often used.", null, 1, 65535);
    public final Setting<String> UPSTREAM_PROXY_HOST = new Setting<>(null, Converter.STRING,
            false, "Name or IP address of the upstream proxy server to send requests to", null, null, null);
    public final Setting<Integer> UPSTREAM_PROXY_PORT = new Setting<>(8080, Converter.INTEGER,
            false, "TCP Port of upstream proxy server to send requests to", null, 1, 65535);

    /* Authentication - these 3 are handled slightly differently */
    public final Setting<String> USERNAME = new Setting<>(System.getenv("USERNAME"), Converter.STRING,
            true, "Username for authenticating to upstream proxy server", null, null, null);
    public final Setting<String> PASSWORD = new Setting<>(null, Converter.STRING,
            true, "Password for authenticating to upstream proxy server", null, null, null);
    public final Setting<Boolean> SAVE_PASS = new Setting<>(false, Converter.YES_NO,
            true, "Save proxy username and password to configuration file?", null, null, null);

    public final String FILE_NAME = "proxyauth.properties";


    /**
     * Intended for internal use only, such as loading configuration.
     * Lists all configuration fields (i.e. all final fields of type Configuration)
     */
    Map<String, Setting<?>> getAllConfigFields() {
        Map<String, Setting<?>> allProps = new HashMap<>();
        for (Field f : Configuration.class.getDeclaredFields()) {
            int modifiers = f.getModifiers();
            if (Modifier.isFinal(modifiers) && f.getType() == Setting.class) {
                try {
                    Setting<?> c = (Setting<?>) f.get(this);
                    if (c == null) throw new IllegalStateException("Configuration field " + f.getName() + " is null");
                    allProps.put(f.getName(), c);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return allProps;
    }


    public static void init(boolean doWizard) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        final Map<String, Setting<?>> allConfigFields = Configuration.getAllConfigFields();
        for (String key : allConfigFields.keySet()) {
            final Setting<?> config = allConfigFields.get(key);
            if ((config.currentValue == null || doWizard) && config != PASSWORD && config != USERNAME && config != SAVE_PASS) {
                config.prompt(key, br);
            }
        }

                if (USERNAME.getValue() == null || PASSWORD.getValue() == null || doWizard) {
                    String currentUser = USERNAME.currentValue;
                    if (currentUser == null) {
                        System.out.println("Username:");
                    } else {
                        System.out.println("Username (press ENTER for '" + currentUser + "'):");
                    }
                    String username = br.readLine();
                    if (username.length() == 0 && currentUser != null) {
                        username = currentUser;
                    }
                    USERNAME.setValue(username);
                    System.out.println("Password (masked)");
                    PASSWORD.setValue(new String(con.readPassword()));

                    if (doSave) {
                        System.out.println("Configuration file location: " + new File(FILE_NAME).getCanonicalPath());
                        SAVE_PASS.prompt("SAVE_PASS", con);
                    }
                }
                if (doSave) save();
            }
        }
    }

}

