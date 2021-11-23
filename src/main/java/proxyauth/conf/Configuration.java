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

package proxyauth.conf;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Manages all user configurable settings for ProxyAuth
 *
 * @author Zeckie
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
    public final Setting<Boolean> CONNECTION_CLOSE = new Setting<>(true, Converter.YES_NO, false,
            "Add headers to indicate the connection needs to be closed. Should be set to Yes to work around issue 23.",
            null, null, null);

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
            false, "TCP port to listen on. Port 8080 is often used.", null, 0, 65535);
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

    void load(boolean required) throws IOException {
        File configFile = new File(FILE_NAME);
        if (!configFile.exists()) {
            if (required) {
                throw new FileNotFoundException("Configuration file " + FILE_NAME + " not found");
            }
        } else {
            Properties props = new Properties();
            props.load(new FileReader(FILE_NAME));
            final Map<String, Setting<?>> allConfigFields = getAllConfigFields();
            for (String key : props.stringPropertyNames()) {
                try {
                    if (allConfigFields.containsKey(key)) {
                        allConfigFields.get(key).setString(props.getProperty(key));
                    } else {
                        System.err.println("Discarding unknown setting from properties file: " + key);
                    }
                } catch (Exception ex) {
                    System.err.println("Unable to load " + key + ": " + ex);
                }
            }
        }
    }

    void save() throws IOException {
        final Map<String, Setting<?>> allConfigFields = getAllConfigFields();
        Properties props = new Properties();
        for (String key : allConfigFields.keySet()) {
            final Setting<?> setting = allConfigFields.get(key);
            final Object val = setting.currentValue;
            if (val != null && (!setting.special || SAVE_PASS.getValue())) {
                props.setProperty(key, setting.toUserString());
            } else {
                System.out.println("Skip save: " + key);
            }
        }
        props.store(new FileWriter(FILE_NAME), "");
    }

    public void init(boolean doLoad, boolean doSave, boolean doWizard, boolean quiet, Console con) throws IOException {
        final Map<String, Setting<?>> allConfigFields = getAllConfigFields();

        if (doLoad) load(false);
        if (!quiet) {
            {
                for (String key : allConfigFields.keySet()) {
                    final Setting<?> config = allConfigFields.get(key);
                    if ((config.currentValue == null || doWizard) && !config.special) {
                        config.prompt(key, con);
                    }
                }

                if (USERNAME.getValue() == null || PASSWORD.getValue() == null || doWizard) {
                    String currentUser = USERNAME.currentValue;
                    if (currentUser == null) {
                        System.out.println("Username:");
                    } else {
                        System.out.println("Username (press ENTER for '" + currentUser + "'):");
                    }
                    String username = con.readLine();
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

        // Check that all setting have values (eg. if running in quiet mode)
        for (String key : allConfigFields.keySet()) {
            if (allConfigFields.get(key).getValue() == null) {
                throw new InvalidSettingException("Setting " + key + " is not set");
            }
        }

    }

}

