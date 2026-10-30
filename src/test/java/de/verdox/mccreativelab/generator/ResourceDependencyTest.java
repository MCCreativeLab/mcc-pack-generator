package de.verdox.mccreativelab.generator;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ResourceDependencyTest {
    @TempDir Path directory;
    static class Pack extends CustomPack<Pack> {
        Pack(Path directory) {
            super("nested", PackFormat.RESOURCE_26_3, "Test", AssetPath.buildPath(directory.toString()),
                directory.resolve("templates").toFile(), directory.resolve("config").toFile());
        }
        @Override protected void createDescriptionFile() {}
        @Override public String mainFolder() { return "assets"; }
    }
    @Test
    void preparesEveryNestedResourceOnceBeforeInstallation() throws Exception {
        var pack = new Pack(directory);
        List<String> events = new ArrayList<>();
        class Dependency extends AbstractResource<Pack> {
            final String name;
            final Dependency dependency;
            Dependency(String name, Dependency dependency) { super(Key.key("test", name)); this.name=name; this.dependency=dependency; }
            @Override public void beforeResourceInstallation(Pack target) {
                events.add("prepare " + name);
                if (dependency != null) target.registerIfNotAlready(dependency);
            }
            @Override public void installResourceToPack(Pack target) { events.add("install " + name); }
        }
        var texture = new Dependency("texture", null);
        var font = new Dependency("font", texture);
        var hud = new Dependency("hud", font);
        pack.register(new Dependency("menu", hud));
        pack.installPack(false);
        assertEquals(List.of("prepare menu", "prepare hud", "prepare font", "prepare texture",
                "install menu", "install hud", "install font", "install texture"), events);
    }
}
