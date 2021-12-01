plugins {
    java
    id("org.jetbrains.changelog") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform() // JUnit 5
}

version = "0.1.1"
group = "com.github.zeckie"

/*
* Work out current java version
* Note: versions prior to JEP223 (Java 9) had version numbers starting with "1." so
* will be counted as major version 1.
*/
val MIN_JAVA_VER = 9
val fullVersion = System.getProperty("java.version")
val majorVersion = Integer.parseInt(fullVersion.substring(0, fullVersion.indexOf('.')))
System.out.println("Java major version: " + majorVersion)


if (majorVersion < MIN_JAVA_VER) {
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(MIN_JAVA_VER))
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
        description = "Run test suite with ${project.name}'s minimum supported java version ($MIN_JAVA_VER). " +
                "Checks that the code is compatible with, and compiled with the correct options to be run on that version"
        group = "verification"
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(MIN_JAVA_VER))
        })
    }
    tasks.check {
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