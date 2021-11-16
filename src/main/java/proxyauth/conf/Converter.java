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

import java.util.Set;

/**
 * Convert between user readable / supplied string, and another type
 *
 * @author Zeckie
 */
abstract class Converter<A> {

    abstract A fromString(String val) throws InvalidSettingException;

    String toString(A val) {
        return val.toString();
    }

    private static final Set<String> YES = Set.of("yes", "y", "true");
    private static final Set<String> NO = Set.of("no", "n", "false");

    public static final Converter<Boolean> YES_NO = new Converter<>() {
        @Override
        Boolean fromString(String val) throws InvalidSettingException {
            String lower = val.toLowerCase();
            if (YES.contains(lower)) {
                return true;
            }
            if (NO.contains(lower)) {
                return false;
            }
            throw new InvalidSettingException("Value should be 'Yes' or 'No'");
        }

        @Override
        String toString(Boolean val) {
            return val ? "Yes" : "No";
        }
    };
    static final Converter<Integer> INTEGER = new Converter<>() {
        @Override
        Integer fromString(String val) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                throw new InvalidSettingException("Value should be an integer", nfe);
            }
        }
    };
    static final Converter<String> STRING = new Converter<>() {
        @Override
        String fromString(String val) {
            return val;
        }
    };

    private Converter() {
    }
}
