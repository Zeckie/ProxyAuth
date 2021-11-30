plugins {
    java
    id("org.jetbrains.changelog") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

version = "0.1.1"
group = "com.github.zeckie"

// Work out current java version
val MIN_JAVA_VER = 9
val fullVersion = System.getProperty("java.version")
val majorVersion = Integer.parseInt(fullVersion.substring(0, fullVersion.indexOf('.')))
System.out.println("Java major version: " + majorVersion)

if (majorVersion < MIN_JAVA_VER) {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(MIN_JAVA_VER))
        }
    }
} else {
    if (majorVersion > 9) {
        // Compile for minimum supported java version
        // skip for java 9 due to https://bugs.openjdk.java.net/browse/JDK-8139607
        tasks.withType<JavaCompile> {
            options.release.set(MIN_JAVA_VER)
        }
    }

    // Test on minimum supported java version
    tasks.register<Test>("testsMinJava") {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(MIN_JAVA_VER))
        })
    }
    tasks.build {
        dependsOn("testsMinJava")
    }
}

// set main class
tasks.jar {
    manifest.attributes["Main-Class"] = "proxyauth.Main"
}

changelog {
    groups.set(listOf("Added"))
}
