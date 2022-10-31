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

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Compact logging format (usually single line) to display as console output
 *
 * @author Zeckie
 */
public class CompactFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        final Throwable thrown = record.getThrown();
        String exMsg = (thrown == null) ? "" :
                (": " + thrown.getClass() + ": " + thrown.getLocalizedMessage());
        return String.format("%1$tl:%1$tM:%1$tS %1$Tp [%2$s] %3$s%4$s%n",
                new Date(record.getMillis()),
                record.getLevel(),
                formatMessage(record),
                exMsg
        );
    }
}
