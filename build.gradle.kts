import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")

    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2025.3.6.1")
        bundledPlugin("Git4Idea")
        testFramework(TestFrameworkType.Platform)
    }
}
