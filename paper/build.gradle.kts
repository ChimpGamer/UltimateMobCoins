repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.12.1") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.12.1") { isTransitive = false }
}

tasks {
    jar {
        enabled = false
    }
}