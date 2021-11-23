plugins {
    java
    application
    id("org.jetbrains.changelog") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
}

tasks.test {
    useJUnit()
}

version = "0.1.0"
group = "com.github.zeckie"

val MIN_JAVA_VER = 9
if (Runtime.version().feature() < MIN_JAVA_VER) {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(MIN_JAVA_VER))
        }
    }
}

tasks.compileJava {
    options.release.set(MIN_JAVA_VER)
}

// set main class
"proxyauth.Main".let { main ->
    application {
        mainClass.set(main)
    }

    tasks.jar {
        manifest.attributes["Main-Class"] = main
    }
}

changelog {
    groups.set(listOf("Added"))
}
