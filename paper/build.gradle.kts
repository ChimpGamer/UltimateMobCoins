repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.rosewooddev.io/repository/public/") // RoseStacker repository

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://nexus.betonquest.org/repository/betonquest/") // BetonQuest Repository

    maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault Repository

    maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs Repository

    maven("https://repo.oraxen.com/releases") // Oraxen Repository

    maven("https://jitpack.io") // Used for ItemsAdder

    maven("https://repo.auxilor.io/repository/maven-public/") // Eco Repository

    maven("https://maven.enginehub.org/repo/") // WorldGuard Repository

    maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")

    compileOnly("dev.rosewood:rosestacker:1.5.30")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("io.lumine:Mythic-Dist:5.2.1") // Mythic Mobs API
    compileOnly("io.th0rgal:oraxen:1.189.0")

    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.5.0b")
    compileOnly("com.willfp:eco:6.68.6")
    compileOnly("com.willfp:EcoMobs:10.19.0")
    compileOnly("org.betonquest:betonquest:2.2.1") { isTransitive = false }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8") // WorldGuard
    compileOnly("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.13")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.16.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.16.0") { isTransitive = false }

    compileOnly(platform("org.mongodb:mongodb-driver-bom:5.4.0"))
    compileOnly("org.mongodb:mongodb-driver-kotlin-coroutine") {
        exclude("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    }
    compileOnly("org.mongodb:bson-kotlinx:5.4.0")

    implementation("org.bstats:bstats-bukkit:3.0.2")
}