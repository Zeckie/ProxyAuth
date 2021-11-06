## Contributing

To find our what this project is about, read the [README.md](README.md)

Via issues / pull requests - https://github.com/Zeckie/ProxyAuth/

Significant changes / new features should be [discussed first](https://github.com/Zeckie/ProxyAuth/discussions) before
implementing.

## Building

### With Gradle

ProxyAuth can be built using Gradle, and includes the
[Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). Note that Gradle has different
requirements, such as not currently supporting Java 17 or later.

If you are building in an environment under proxy you might need
to [configure Gradle's proxy settings](https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy)

The project is configured to use [Gradle Toolchains](https://docs.gradle.org/current/userguide/toolchains.html), so any
[supported Java version](https://docs.gradle.org/current/userguide/compatibility.html) will be sufficient to complete the build.

To compile, execute tests, and generate the executable jar, run:

```bash
./gradlew build
```
(note: `cmd` is incompatible with `./gradlew`, use `gradlew build`)

This will create the jar file in `build/libs` (e.g. `build/libs/ProxyAuth-0.1.0.jar`)

### Without Gradle

It does not rely on any third-party libraries, so can be built using any Java IDE, or even javac.

`C:\example\ProxyAuth\src\main\java>javac -d ../bin proxyauth/*.java`

## Design

The main language of the project is Java (NOT Kotlin!).

### Dependencies

Dependencies for build / test (such as JUnit) are managed using Gradle. All dependencies should be current, stable
versions.
