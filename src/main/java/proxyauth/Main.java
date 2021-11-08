package proxyauth;

import java.io.IOException;

/**
 * A simple proxy server to authenticate to another proxy server.
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(LICENCE);

        Configuration.init();
        new ProxyListener().run();
    }

    static final String LICENCE = "---\n" +
            "Copyright (C) 2021  Zeckie - https://github.com/Zeckie/\n" +
            "\n" +
            "This program is free software: you can redistribute it and/or modify\n" +
            "it under the terms of the GNU General Public License as published by\n" +
            "the Free Software Foundation, either version 3 of the License, or\n" +
            "(at your option) any later version.\n" +
            "\n" +
            "This program is distributed in the hope that it will be useful,\n" +
            "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
            "GNU General Public License for more details.\n" +
            "\n" +
            "You should have received a copy of the GNU General Public License\n" +
            "along with this program.  If not, see <https://www.gnu.org/licenses/>.\n" +
            "---\n";
}
