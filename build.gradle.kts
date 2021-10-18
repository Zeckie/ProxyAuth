plugins {
    java
    application
    id("org.jetbrains.qodana") version "0.1.12"
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

// Configure Gradle Qodana Plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(buildDir.resolve("reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT").toBoolean())
}
