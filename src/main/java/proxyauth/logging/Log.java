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

package proxyauth.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Logging using  java.util.logging
 *
 * @author Zeckie
 */
public class Log {
    private static final LogManager LOG_MANAGER = LogManager.getLogManager();
    private static final Set<Logger> LOGGERS = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(Log.class.getCanonicalName());

    private static final String LOGGING_FILENAME = "logging.properties";

    static String loggerInfo(Logger logger) {
        Level lvl = logger.getLevel();
        String name = logger.getName();
        return (name.isEmpty() ? "(root)" : "name=\"" + logger.getName() + "\"") +
                ", level=" + (lvl == null ? "(inherit)" : lvl) +
                ", use parent handlers: " + logger.getUseParentHandlers() +
                ", handlers=" + Arrays.toString(logger.getHandlers());
    }

    /* Initialize logging.
     * Loads configuration from logging.properties from:
     *  - current working directory
     *  - user home
     *  - inside the jar
     */
    public static void init() {
        try {
            // Try current working directory, user home, internal
            if (!(
                    load(new File(LOGGING_FILENAME))
                            || load(new File(System.getProperty("user.home"), LOGGING_FILENAME))
            ))
                load();

            if (LOGGER.isLoggable(Level.FINE)) {
                StringBuilder existing = new StringBuilder();
                existing.append("Currently loaded loggers:");
                List<String> names = new ArrayList<>(Collections.list(LOG_MANAGER.getLoggerNames()));
                names.sort(null);
                for (String name : names) {
                    final Logger logger = LOG_MANAGER.getLogger(name);
                    if (logger == null) {
                        // unlikely but possible
                        existing.append("\n - Logger ").append(name).append(" is null");
                    } else {
                        existing.append("\n -").append(loggerInfo(logger));
                    }
                }
                LOGGER.fine(existing.toString());
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize logger", ex);
        }
    }

    private static void load() throws IOException {
        final URL resource = Log.class.getResource("logging.properties");
        if (resource == null) {
            LOGGER.severe("Logging configuration file missing, using java defaults");
        } else {
            load(resource.toString(), Log.class.getResourceAsStream("logging.properties"));
        }
    }

    private static void load(String source, InputStream stream) throws IOException {
        LOGGER.fine("Loading logging configuration from: " + source);
        LOG_MANAGER.updateConfiguration(stream, null);
        LOGGER.info("Loaded logging configuration from: " + source);
    }

    private static boolean load(File file) throws IOException {
        final String absolutePath = "\"" + file.getAbsolutePath() + "\"";

        if (file.exists()) {
            load(absolutePath, new FileInputStream(file));
            return true;
        } else {
            LOGGER.fine("Logging configuration does not exist: " + absolutePath);
            return false;
        }
    }

    /**
     * Create logger using same name as supplied class
     */
    public static Logger logger(Class<?> c) {
        final String name = c.getCanonicalName();

        Logger retval = Logger.getLogger(name);
        if (LOGGERS.add(retval)) {
            LOGGER.log(Level.FINE, "Added logger: " + loggerInfo(retval));
        }
        return retval;
    }

}
