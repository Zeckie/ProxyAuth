import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.changelog") version "1.3.1"
}

version = "0.1.1"
group = "com.github.zeckie"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

val minimumJavaVersion = JavaVersion.VERSION_1_9

if (JavaVersion.current() < minimumJavaVersion) {
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(minimumJavaVersion.majorVersion))
} else {
    if (JavaVersion.current() > JavaVersion.VERSION_1_9) {
        // Compile for minimum supported java version
        // skip for java 9 due to https://bugs.openjdk.java.net/browse/JDK-8139607
        tasks.withType<JavaCompile> {
            options.release.set(minimumJavaVersion.majorVersion.toInt())
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = minimumJavaVersion.majorVersion.takeUnless { it == "8" } ?: "1.8"
    }

    tasks.test {
        dependsOn(testsMinJava)
    }
}

// Test on minimum supported java version
val testsMinJava by tasks.registering(Test::class) {
    group = "verification"
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(minimumJavaVersion.majorVersion))
    })
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// set main class
tasks.jar {
    manifest.attributes["Main-Class"] = "proxyauth.Main"
}

changelog {
    groups.set(listOf("Added"))
}

val ktTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output

}

val ktTestImplementation by configurations.getting

kotlin.sourceSets["test"].kotlin.setSrcDirs(listOf<Any>())
kotlin.sourceSets["ktTest"].kotlin.setSrcDirs(listOf<Any>(file("src/test/kotlin")))

dependencies {
    ktTestImplementation(kotlin("test-junit5"))
}

val kotlinTest by tasks.registering(Test::class) {
    description = "Runs test written in Kotlin."
    group = "verification"

    testClassesDirs = ktTest.output.classesDirs
    classpath = ktTest.runtimeClasspath
}

tasks.test { dependsOn(kotlinTest) }
