# ProxyAuth

ProxyAuth is a simple forwarding http proxy server, written in Java intended to run on a desktop computer and forward
those requests to an upstream proxy server that requires authentication.

The typical use case is inside an organization that requires users to authenticate to a corporate proxy server.

Some applications need proxy settings (including password) to be set in
[non-standardized](https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/) environment variables such
as `HTTP_PROXY`, `http_proxy`, `https_proxy` and `no_proxy`. Other applications need proxy details to be configured in a
configuration file specific to that application, or passed as command-line arguments. When you change your password, it
needs to be updated in lots of places, and failing to update may result in accounts being locked. ProxyAuth is intended
to be a single place to configure proxy authentication for those applications.

ProxyAuth is not intended to be a general purpose proxy server, and does not cache, transform or modify the content
(body) of requests. It also does not aim to be the fastest, or most secure.

## Dependencies

Java (version 9 or later). No third-party libraries required for execution.

## Running

ProxyAuth does not have a GUI, and should be run from a console / shell such as Command Prompt (`cmd.exe`), `bash`, or
even Powershell.

```
java -jar ProxyAuth-0.1.0.jar
```

The first time it is run, ProxyAuth will prompt for:

- UPSTREAM_PROXY_HOST (Name or IP address of the upstream proxy server to send requests to)
- USERNAME (Username for authenticating to upstream proxy server)
- PASSWORD (Password for authenticating to upstream proxy server)
- SAVE_PASS (Should password be saved to configuration file?)

By default, it will listen on local address 127.0.0.127 port 8080.

```
Listening ServerSocket[addr=/127.0.0.127,localport=8080]
```

You can now configure other applications to use this proxy.

### Configuring

The configuration file `proxyauth.properties` is created automatically in the working directory.

To run the configuration wizard:

```
java -jar ProxyAuth-0.1.0.jar -wizard
```

Which will show each setting, including a description of what it does. eg.

```
LISTEN_BACKLOG: Number of incoming connections that can be queued. Setting this too low will result in connections being refused

Enter LISTEN_BACKLOG (press ENTER for 50):



STOP_ON_PROXY_AUTH_ERROR: Immediately stop on http error 407, to prevent account from being locked due to multiple attempts with wrong password

Enter STOP_ON_PROXY_AUTH_ERROR (press ENTER for Yes):



LISTEN_PORT: TCP port to listen on. Port 8080 is often used.

Enter LISTEN_PORT (press ENTER for 8080):
...
```

## Copyright and Licence

Copyright (C) 2021 Zeckie - https://github.com/Zeckie/

ProxyAuth is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ProxyAuth is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

A copy of the GNU General Public License should be available in the file [LICENSE](LICENSE). If not,
see <https://www.gnu.org/licenses/>.

## Building and Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)
