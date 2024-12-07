import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import xyz.wagyourtail.commons.gradle.shadow.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.kotlin)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":"))

    implementation(libs.commons.kt)

    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    implementation(compose.desktop.currentOs)

    implementation(libs.kotlin.serialization.json)

}

val shadowJar by tasks.registering(ShadowJar::class) {
    from(sourceSets.main.get().output)

    archiveBaseName.set(base.archivesName.get() + "-editor")
    archiveClassifier = "all"
    shadowContents.add(configurations.runtimeClasspath.get())

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("module-info.class")

    manifest {
        attributes(
            "Main-Class" to "xyz.wagyourtail.konig.editor.MainKt"
        )
    }
}

tasks.assemble {
    dependsOn(shadowJar)
}

compose.desktop {
    application {
        mainClass = "xyz.wagyourtail.konig.editor.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "konig"
            packageVersion = version.toString()
        }
    }
}

