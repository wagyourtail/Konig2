pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.wagyourtail.xyz/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }

}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include("editor")

rootProject.name = "Konig2"

