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

    @NotNull
    private static ArrayList<String> getLibraries() {
        var dependencies = new ArrayList<String>();
        dependencies.add("org.jetbrains.kotlin:kotlin-stdlib:2.3.21");
        dependencies.add("org.jetbrains.exposed:exposed-core:1.3.0");
        dependencies.add("org.jetbrains.exposed:exposed-dao:1.3.0");
        dependencies.add("org.jetbrains.exposed:exposed-jdbc:1.3.0");
        dependencies.add("org.xerial:sqlite-jdbc:3.49.1.0");
        dependencies.add("org.mariadb.jdbc:mariadb-java-client:3.5.8");
        dependencies.add("org.incendo:cloud-core:2.1.0");
        dependencies.add("org.incendo:cloud-paper:2.0.0");
        dependencies.add("org.incendo:cloud-minecraft-extras:2.0.0");
        dependencies.add("org.incendo:cloud-kotlin-coroutines:2.1.0");
        dependencies.add("dev.dejvokep:boosted-yaml:1.3.7");
        dependencies.add("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.7.0");
        dependencies.add("com.github.ben-manes.caffeine:caffeine:3.2.4");
        dependencies.add("com.zaxxer:HikariCP:7.0.2");
        dependencies.add("org.postgresql:postgresql:42.7.5");
        dependencies.add("org.mongodb:mongodb-driver-core:5.7.0");
        dependencies.add("org.mongodb:mongodb-driver-kotlin-coroutine:5.7.0");
        dependencies.add("org.mongodb:bson-kotlinx:5.7.0");
        dependencies.add("org.mongodb:mongodb-driver-reactivestreams:5.7.0");
        dependencies.add("org.reactivestreams:reactive-streams:1.0.4");
        dependencies.add("io.github.g00fy2:versioncompare:1.5.0");
        dependencies.add("net.kyori:adventure-text-feature-pagination:5.0.1-SNAPSHOT");
        return dependencies;
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var dependencies = getLibraries();

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("networkmanager", "default", "https://repo.networkmanager.xyz/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}