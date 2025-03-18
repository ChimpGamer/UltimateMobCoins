repositories {
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.rosewooddev.io/repository/public/") // RoseStacker repository
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")

    compileOnly("dev.rosewood:rosestacker:1.5.30")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.16.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.16.0") { isTransitive = false }

    implementation("org.bstats:bstats-bukkit:3.0.2")
}