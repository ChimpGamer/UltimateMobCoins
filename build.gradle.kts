import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "nl.chimpgamer.ultimatemobcoins"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

        maven("https://repo.codemc.io/repository/maven-public/") // nbt-item-api repository

        maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault Repository

        maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs Repository

        maven("https://jitpack.io") // Used for Oraxen
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("me.clip:placeholderapi:2.11.3")
        compileOnly("net.milkbowl.vault:VaultAPI:1.7")
        compileOnly("io.lumine:Mythic-Dist:5.0.4") // Mythic Mobs API
        compileOnly("com.github.oraxen:oraxen:1.155.4")
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.4.1-r4")
        implementation("de.tr7zw:item-nbt-api-plugin:2.11.2")
        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("dev.dejvokep:boosted-yaml:1.3.1")
        compileOnly("cloud.commandframework:cloud-paper:1.8.3")
        compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.3")
        compileOnly("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.2")

        compileOnly("org.jetbrains.exposed:exposed-core:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.xerial:sqlite-jdbc:3.41.2.1")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }

        processResources {
            filesMatching("**/*.yml") {
                expand("version" to project.version)
            }
        }

        shadowJar {
            archiveFileName.set("UltimateMobCoins-${project.name.capitalizeWords()}-v${project.version}.jar")

            relocate("de.tr7zw")
            relocate("net.kyori.adventure.text.feature.pagination")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        enabled = false
    }
}

fun ShadowJar.relocate(vararg dependencies: String) {
    dependencies.forEach {
        val split = it.split(".")
        val name = split.last()
        relocate(it, "${project.group}.libs.$name")
    }
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s -> s.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }