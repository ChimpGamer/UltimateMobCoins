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
            add("org.jetbrains.kotlin:kotlin-stdlib:1.9.23");
            add("org.jetbrains.kotlin:kotlin-reflect:1.9.23");
            add("org.jetbrains.exposed:exposed-core:0.48.0");
            add("org.jetbrains.exposed:exposed-dao:0.48.0");
            add("org.jetbrains.exposed:exposed-jdbc:0.48.0");
            add("org.xerial:sqlite-jdbc:3.44.1.0");
            add("org.mariadb.jdbc:mariadb-java-client:3.3.3");
            add("cloud.commandframework:cloud-paper:1.8.4");
            add("cloud.commandframework:cloud-minecraft-extras:1.8.4");
            add("cloud.commandframework:cloud-kotlin-coroutines:1.8.4");
            add("dev.dejvokep:boosted-yaml:1.3.1");
            add("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.8");
            add("com.github.ben-manes.caffeine:caffeine:3.1.8");
        }};

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("networkmanager", "default", "https://repo.networkmanager.xyz/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}