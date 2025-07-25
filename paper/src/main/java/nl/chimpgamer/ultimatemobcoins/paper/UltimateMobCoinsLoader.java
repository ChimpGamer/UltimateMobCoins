package nl.chimpgamer.ultimatemobcoins.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UltimateMobCoinsLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var dependencies = new ArrayList<String>() {{
            add("org.jetbrains.kotlin:kotlin-stdlib:2.2.0");
            add("org.jetbrains.exposed:exposed-core:0.60.0");
            add("org.jetbrains.exposed:exposed-dao:0.60.0");
            add("org.jetbrains.exposed:exposed-jdbc:0.60.0");
            add("org.xerial:sqlite-jdbc:3.49.1.0");
            add("org.mariadb.jdbc:mariadb-java-client:3.5.2");
            add("org.incendo:cloud-core:2.0.0");
            add("org.incendo:cloud-paper:2.0.0-beta.10");
            add("org.incendo:cloud-minecraft-extras:2.0.0-beta.10");
            add("org.incendo:cloud-kotlin-coroutines:2.0.0");
            add("dev.dejvokep:boosted-yaml:1.3.7");
            add("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.13");
            add("com.github.ben-manes.caffeine:caffeine:3.1.8");
            add("com.zaxxer:HikariCP:6.1.0");
            add("org.postgresql:postgresql:42.7.5");

            add("org.mongodb:mongodb-driver-core:5.4.0");
            add("org.mongodb:mongodb-driver-kotlin-coroutine:5.4.0");
            add("org.mongodb:bson-kotlinx:5.4.0");
            add("org.mongodb:mongodb-driver-reactivestreams:5.4.0");
            add("org.reactivestreams:reactive-streams:1.0.4");

            add("io.github.g00fy2:versioncompare:1.5.0");
        }};

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("networkmanager", "default", "https://repo.networkmanager.xyz/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}