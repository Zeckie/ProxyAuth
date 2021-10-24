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

Java (version 16 or later). No third-party libraries required for execution.

## Running

TODO - depends on https://github.com/Zeckie/ProxyAuth/issues/6 (configuration)

ProxyAuth does not have a GUI, and should be run from a console / shell such as Command Prompt (`cmd.exe`), `bash`, or
even Powershell.

```
java -jar ProxyAuth-0.1.0.jar
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
