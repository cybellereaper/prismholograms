package com.example.spectraholograms;

import com.example.spectraholograms.model.*;
import com.example.spectraholograms.repository.YamlHologramRepository;
import com.example.spectraholograms.serialization.SerializationService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryAndSerializationTest {
    @TempDir
    File tempDir;

    @Test
    void saveLoadRoundTrip() {
        File file = new File(tempDir, "holograms.yml");
        SerializationService serialization = new SerializationService();
        YamlHologramRepository repository = new YamlHologramRepository(file, Logger.getAnonymousLogger(), serialization);

        Hologram hologram = new Hologram(
                new HologramId("spawn"),
                new HologramLocation("world", 1, 65, 1, 90f, 0f),
                List.of(serialization.line("&aHello"), serialization.line("&bWorld")),
                new HologramViewSettings(40.0, false),
                new HologramStyle(Display.Billboard.CENTER, true, false, null, null, 1.0f),
                Instant.now(),
                Instant.now()
        );

        repository.saveAll(List.of(hologram));
        List<Hologram> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        assertEquals("spawn", loaded.getFirst().id().value());
        assertEquals(2, loaded.getFirst().lines().size());
    }

    @Test
    void malformedEntryDoesNotCrashLoad() throws IOException {
        File file = new File(tempDir, "holograms.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("holograms.good.world", "world");
        yaml.set("holograms.good.x", 0.0);
        yaml.set("holograms.good.y", 64.0);
        yaml.set("holograms.good.z", 0.0);
        yaml.set("holograms.good.lines", List.of("ok"));
        yaml.set("holograms.bad.x", 10.0);
        yaml.save(file);

        YamlHologramRepository repository = new YamlHologramRepository(file, Logger.getAnonymousLogger(), new SerializationService());
        List<Hologram> loaded = repository.loadAll();

        assertEquals(1, loaded.size());
        assertEquals("good", loaded.getFirst().id().value());
    }
}
