repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.rosewooddev.io/repository/public/") // RoseStacker repository

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.betonquest.org/betonquest/") // BetonQuest Repository

    maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault Repository

    maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs Repository

    maven("https://repo.oraxen.com/releases") // Oraxen Repository

    maven("https://jitpack.io") // Used for ItemsAdder

    maven("https://repo.auxilor.io/repository/maven-public/") // Eco Repository

    maven("https://maven.enginehub.org/repo/") // WorldGuard Repository

    maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository

    maven("https://repo.nexomc.com/releases") // Nexo Repository
}

dependencies {
    val kotlinGroupId = "org.jetbrains.kotlin"
    compileOnly(kotlin("stdlib"))

    compileOnly(libs.adventure.text.feature.pagination) { isTransitive = false }

    compileOnly(libs.boosted.yaml)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.minecraft.extras)
    compileOnly(libs.cloud.kotlin.coroutines)

    compileOnly(libs.exposed.core) {
        exclude(kotlinGroupId)
    }
    compileOnly(libs.exposed.dao) {
        exclude(kotlinGroupId)
    }
    compileOnly(libs.exposed.jdbc) {
        exclude(kotlinGroupId)
    }
    compileOnly(libs.hikaricp)
    compileOnly(libs.sqlite.jdbc)
    compileOnly(libs.mariadb.java.client)
    compileOnly(libs.caffeine)
    compileOnly(libs.versioncompare)

    compileOnly(libs.paper.api)

    compileOnly(libs.miniplaceholders.api)
    compileOnly(libs.miniplaceholders.kotlin.ext)

    compileOnly(libs.cloud.paper)

    compileOnly(libs.rosestacker)
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault.api)
    compileOnly(libs.mythic.dist) // Mythic Mobs API
    compileOnly(libs.oraxen)

    compileOnly(libs.itemsadder.api)
    compileOnly(libs.eco)
    compileOnly(libs.ecomobs)
    compileOnly(libs.betonquest) {
        exclude("dev.faststats.metrics", "bukkit")
        exclude("de.themoep", "minedown-adventure")
    }
    compileOnly(libs.worldguard.bukkit) // WorldGuard
    compileOnly(libs.ryseinventory.plugin)
    compileOnly(libs.nexo)

    compileOnly(platform(libs.mongodb.driver.bom))
    compileOnly(libs.mongodb.driver.kotlin.coroutine) {
        exclude("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    }
    compileOnly(libs.bson.kotlinx)

    implementation(libs.mccoroutine.folia.api) { isTransitive = false }
    implementation(libs.mccoroutine.folia.core) { isTransitive = false }

    implementation(libs.bstats.bukkit)
}