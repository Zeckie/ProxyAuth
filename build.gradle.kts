plugins {
    java
    application
    id("org.jetbrains.changelog") version "1.3.1"
}

version = "0.1.0"
group = "com.github.zeckie"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
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
