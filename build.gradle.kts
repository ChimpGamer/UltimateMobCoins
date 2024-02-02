import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

val exposedVersion = "0.47.0"

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

allprojects {
    group = "nl.chimpgamer.ultimatemobcoins"
    version = "1.0.6"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.github.johnrengelman.shadow")
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

        compileOnly("me.clip:placeholderapi:2.11.3")
        compileOnly("net.milkbowl.vault:VaultAPI:1.7")
        compileOnly("io.lumine:Mythic-Dist:5.2.1") // Mythic Mobs API
        compileOnly("com.github.oraxen:oraxen:1.155.4")
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.5.0b")
        compileOnly("com.willfp:eco:6.68.6")
        compileOnly("com.willfp:EcoMobs:10.0.0")
        compileOnly("org.betonquest:betonquest:2.0.0-SNAPSHOT") { isTransitive = false }
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8") // WorldGuard

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("dev.dejvokep:boosted-yaml:1.3.1")
        compileOnly("cloud.commandframework:cloud-paper:1.8.4")
        compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.4")
        compileOnly("cloud.commandframework:cloud-kotlin-coroutines:1.8.4")
        compileOnly("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.8")

        compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.xerial:sqlite-jdbc:3.44.1.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
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
                    username = project.property("NETWORKMANAGER_NEXUS_USERNAME").toString()
                    password = project.property("NETWORKMANAGER_NEXUS_PASSWORD").toString()
                }
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

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}