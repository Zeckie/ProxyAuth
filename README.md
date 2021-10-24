# ProxyAuth

ProxyAuth is a simple forwarding http proxy server, written in Java intended to run on a desktop computer and forward
those requests to an upstream proxy server that requires authentication.

The typical use case is inside an organization that requires users to authenticate to a corporate proxy server.

Some applications need proxy settings (including password) to be set in
[non-standardized](https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/)  environment variables such
as `http_proxy`,
`HTTPS_PROXY` and `NO_PROXY`. Other applications need proxy details to be configured in a configuration file specific to
that application. ProxyAuth is intended to be a single place to configure proxy authentication for those applications.

ProxyAuth is not intended to be a general purpose proxy server, and does not cache, transform or modify the content (
body) of requests. It also does not aim to be the fastest, or most secure.

## Dependencies

Java (version 16 or later). No third-party libraries required for execution.

Dependencies for build / test (such as JUnit) are managed using Gradle. All dependencies should be current, stable
versions.

## Building

### With Gradle

ProxyAuth can be built using Gradle, and includes the
[Gradle Wrapper](https://docs.gradle.org/7.2/userguide/gradle_wrapper.html). Note that Gradle has different
requirements, such as not currently supporting Java 17 or later.

- On Windows:
  `gradlew build`
- On Linux / Unix:
  `./gradlew build`

Gradle will create the jar file in `build\libs` (e.g. `build\libs\ProxyAuth-0.1.0.jar`)

### Without Gradle

It does not rely on any third-party libraries, so can be built using any Java IDE, or even javac.

`C:\example\ProxyAuth\src\main\java>javac -d ..\bin proxyauth\*.java`

## Installation

TODO - depends on https://github.com/Zeckie/ProxyAuth/issues/6 (configuration)

ProxyAuth does not have a GUI, and should be run from a console / shell such as `cmd.exe`.
`java -jar ProxyAuth-0.1.0.jar`

## Licence

[GNU GPLv3](LICENCE) - https://www.gnu.org/licenses/gpl-3.0.html

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)
