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

package proxyauth.actions;

import proxyauth.ProxyRequest;

import java.io.IOException;

/**
 * Performs an action on a proxy request - eg. forward it
 *
 * @author Zeckie
 */
public interface Action {
    /**
     * Note, this method blocks until the action is complete.
     *
     * @param proxyRequest the request to be actioned
     * @return the action succeeded
     * @throws IOException
     */
    boolean action(ProxyRequest proxyRequest) throws IOException;
}
