import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.text.SimpleDateFormat
import java.util.*

val exposedVersion = "0.60.0"

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "8.3.5"
    `maven-publish`
}

allprojects {
    group = "nl.chimpgamer.ultimatemobcoins"
    version = "1.7.1-SNAPSHOT"

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
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-oss-snapshots"
        }
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("dev.dejvokep:boosted-yaml:1.3.7")
        compileOnly("org.incendo:cloud-core:2.0.0")
        compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
        compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0")

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
        compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.2")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
        compileOnly("io.github.g00fy2:versioncompare:1.5.0")
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
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun getDate(): String {
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val date = Date()
    return simpleDateFormat.format(date)
}