import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

val exposedVersion = "0.56.0"

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    `maven-publish`
}

allprojects {
    group = "nl.chimpgamer.ultimatemobcoins"
    version = "1.3.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.gradleup.shadow")
        plugin("maven-publish")
    }

    repositories {
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

        maven("https://nexus.betonquest.org/repository/betonquest/") // BetonQuest Repository

        maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault Repository

        maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs Repository

        maven("https://jitpack.io") // Used for Oraxen

        maven("https://repo.auxilor.io/repository/maven-public/") // Eco Repository

        maven("https://maven.enginehub.org/repo/") // WorldGuard Repository

        maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("net.milkbowl.vault:VaultAPI:1.7")
        compileOnly("io.lumine:Mythic-Dist:5.2.1") // Mythic Mobs API
        compileOnly("com.github.oraxen:oraxen:1.155.4")
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.5.0b")
        compileOnly("com.willfp:eco:6.68.6")
        compileOnly("com.willfp:EcoMobs:10.0.0")
        compileOnly("org.betonquest:betonquest:2.1.3") { isTransitive = false }
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8") // WorldGuard

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("dev.dejvokep:boosted-yaml:1.3.7")
        compileOnly("org.incendo:cloud-core:2.0.0")
        compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
        compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0")
        compileOnly("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.13")

        compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("com.zaxxer:HikariCP:6.1.0")
        compileOnly("org.xerial:sqlite-jdbc:3.47.0.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.1")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    publishing {
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
        repositories {
            maven {
                name = "nexus"
                url = uri("https://repo.networkmanager.xyz/repository/maven-ultimatemobcoins/")
                credentials {
                    username = project.findProperty("NETWORKMANAGER_NEXUS_USERNAME").toString()
                    password = project.findProperty("NETWORKMANAGER_NEXUS_PASSWORD").toString()
                }
            }
        }
    }

    tasks {

        processResources {
            filesMatching("**/*.yml") {
                expand("version" to project.version)
            }
        }

        shadowJar {
            archiveFileName.set("UltimateMobCoins-${project.name.capitalizeWords()}-v${project.version}.jar")

            //relocate("de.tr7zw")
            relocate("net.kyori.adventure.text.feature.pagination")
            relocate("org.bstats")
            relocate("com.github.shynixn.mccoroutine")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

tasks {
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

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}