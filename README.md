# ProxyAuth

ProxyAuth is a simple forwarding http proxy server, written in Java intended to run on a desktop computer and forward
those requests to an upstream proxy server that requires authentication.

The typical use case is inside an organization that requires users to authenticate to a corporate proxy server.

Some applications need proxy settings (including password) to be set in
[non-standardized](https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/)  environment variables such as
`http_proxy`, `https_proxy` and `no_proxy`. Other applications need proxy details to be configured in a configuration
file specific to that application. ProxyAuth is intended to be a single place to configure proxy authentication for those
applications.

ProxyAuth is not intended to be a general purpose proxy server, and does not cache, transform or modify the content (
body) of requests. It also does not aim to be the fastest, or most secure.

## Dependencies

Java (version 16 or later). No third-party libraries required for execution.

## Running

TODO - depends on https://github.com/Zeckie/ProxyAuth/issues/6 (configuration)

ProxyAuth does not have a GUI, and should be run from a console / shell such as `bash` / `powershell`.

```bash
java -jar ProxyAuth-0.1.0.jar
```

## Licence

[GNU GPLv3](LICENSE) - https://www.gnu.org/licenses/gpl-3.0.html

## Contributing / Building

See [CONTRIBUTING.md](CONTRIBUTING.md)
