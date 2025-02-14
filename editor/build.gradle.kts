import xyz.wagyourtail.commons.gradle.shadow.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":"))

    implementation(libs.commons)
    implementation(libs.commons.kt)

    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    implementation(libs.imgui.java.app)
    implementation(libs.imgui.natives.linux)
    implementation(libs.imgui.natives.windows)

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
