import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.text.SimpleDateFormat
import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    `maven-publish`
}

allprojects {
    group = "nl.chimpgamer.ultimatemobcoins"
    version = "2.1.1"

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
        mavenLocal()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    kotlin {
        jvmToolchain(21)
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
            val buildNumber = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"
            filesMatching("**/*.yml") {
                expand("version" to project.version, "buildDate" to getDate(), "buildNumber" to buildNumber)
            }
        }

        shadowJar {
            manifest {
                attributes["paperweight-mappings-namespace"] = "mojang"
            }

            archiveFileName.set("UltimateMobCoins-${project.name.capitalizeWords()}-v${project.version}.jar")

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
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun getDate(): String {
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val date = Date()
    return simpleDateFormat.format(date)
}