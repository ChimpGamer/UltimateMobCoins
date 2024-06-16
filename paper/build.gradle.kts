repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    compileOnly("org.incendo:cloud-paper:2.0.0-beta.8")

    /*implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0") { isTransitive = false }*/
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.15.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.15.0") { isTransitive = false }

    implementation("org.bstats:bstats-bukkit:3.0.2")
}