package proxyauth;

import proxyauth.conf.Configuration;

import java.io.IOException;

/**
 * A simple proxy server to authenticate to another proxy server.
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Main {
    public static void main (String[] args) throws IOException {
        System.out.println(LICENCE);
        Configuration.init(true);

        final Configuration configuration = new Configuration();
        configuration.init(false);

        new ProxyListener(configuration).run();
    }

    static final String LICENCE="""
    ---
    Copyright (C) 2021  Zeckie - https://github.com/Zeckie/
   
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
    ---
    """;

}
