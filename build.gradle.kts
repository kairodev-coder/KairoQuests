import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.yaml.snakeyaml.Yaml

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.5")
    }
}

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "dev.kairo"
version = "1.0.0"
val pluginVersion = version.toString()

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation(kotlin("stdlib"))
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")
    implementation("com.zaxxer:HikariCP:7.0.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to pluginVersion)
    }
}

tasks.register("validateYaml") {
    inputs.files(fileTree("src/main/resources") { include("*.yml") })
    doLast {
        val yaml = Yaml()
        inputs.files.files.forEach { file ->
            file.inputStream().use { yaml.loadAll(it).toList() }
        }
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    minimize {
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
        exclude(dependency("com.zaxxer:HikariCP:.*"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:.*"))
    }
}

tasks.build {
    dependsOn("validateYaml", "shadowJar")
}
