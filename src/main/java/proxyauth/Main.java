package proxyauth;

import proxyauth.conf.Configuration;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple proxy server to authenticate to another proxy server.
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Main {

    /**
     * ProxyAuth entry point
     */
    public static void main(String[] args) throws IOException {

        boolean argErrs = parseArgs(args);
        if (!OPT_QUIET.set) {
            System.out.println(COPYRIGHT);
        }

        if (OPT_HELP.set) {
            System.out.println("\nProxyAuth supports the following command line switches:\n");
            for (Option opt : OPTIONS) {
                System.out.println(opt.helpMsg);
            }
            System.out.println(LONG_HELP);
            System.exit(argErrs ? -1 : 0);
        } else {
            System.out.println(SHORT_HELP);
        }

        if (OPT_LICENCE.set) {
            System.out.println("TODO");
            System.exit(0);
        }

        if (OPT_QUIET.set && OPT_WIZARD.set) {
            System.err.println("WARN: wizard option will be ignored as quiet option specified");
            OPT_WIZARD.set = false;
        }

        Console con = System.console();
        if (con == null && !OPT_NO_CONSOLE.set && !OPT_QUIET.set) {
            System.err.println("No console detected.\n" +
                    "ProxyAuth should be run from command prompt / interactive console, or with /quiet switch.");

        }

        final Configuration configuration = new Configuration();
        configuration.init(!OPT_RESET.set, !OPT_NO_SAVE.set, OPT_WIZARD.set, OPT_QUIET.set, con);

        new ProxyListener(configuration).run();
    }

    private static boolean parseArgs(String[] args) {
        boolean err = false;
        for (String arg : args) {
            boolean found = false;
            String cleaned = Option.clean(arg);
            for (Option opt : OPTIONS) {
                if (opt.args.contains(cleaned)) {
                    opt.set();
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.err.println("ERROR: Unrecognised switch: " + arg);
                OPT_HELP.set();
                err = true;
            }
        }
        return err;
    }

    static final String COPYRIGHT = """
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
    static final String SHORT_HELP = "For help (including configuration and licence) run with command line switch /? or --help";
    static final String LONG_HELP = "\nFor more, see https://github.com/Zeckie/ProxyAuth\n";

    static final Option OPT_HELP = new Option("Displays this list of commands (then quits)", "help", "" /* eg. for "-?" or "/?" */, "h");
    static final Option OPT_LICENCE = new Option("Display copyright and licence (then quits)", "licence", "l", "copyright", "gpl");
    static final Option OPT_WIZARD = new Option("Runs configuration wizard", "wizard", "config", "w");
    static final Option OPT_RESET = new Option("Skips loading of configuration file", "reset", "noload", "r");
    static final Option OPT_NO_SAVE = new Option("Skips saving of configuration file", "nosave", "n");
    static final Option OPT_QUIET = new Option("Disables console input and most console output. Will quit if required settings not configured.", "quiet", "q");
    static final Option OPT_NO_CONSOLE = new Option("Skips attempt to launch interactive console if launched non-interactively", "noconsole");

    //TODO: implement OPT_LICENCE
    //TODO: use OPT_QUIET more

    static final List<Option> OPTIONS = List.of(OPT_HELP, OPT_LICENCE, OPT_WIZARD, OPT_RESET, OPT_NO_SAVE, OPT_QUIET, OPT_NO_CONSOLE);
}

class Option {
    final Set<String> args;
    final String helpMsg;
    boolean set = false;

    /**
     * @param helpMsg description of this option to be displayed when user requests help
     * @param args    clean versions of command line arguments that trigger this option
     */
    Option(String helpMsg, String... args) {
        this.helpMsg = args[0] + " - " + helpMsg;
        this.args = Set.of(args);
    }

    /**
     * Clean up argument by converting to lowercase and remove non-alphanumeric chars,
     * so windows ("/foo"), *nix ("--foo") or bare ("foo") format can be used
     *
     * @param arg
     * @return cleaned version
     */
    public static String clean(String arg) {
        return arg.toLowerCase(Locale.ROOT).replaceAll("\\W", "");
    }

    public void set() {
        this.set = true;
    }
}