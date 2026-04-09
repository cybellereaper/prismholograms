package com.example.spectraholograms;

import com.example.spectraholograms.config.ConfigService;
import com.example.spectraholograms.config.PluginSettings;
import com.example.spectraholograms.model.Hologram;
import com.example.spectraholograms.model.HologramLocation;
import com.example.spectraholograms.repository.HologramRepository;
import com.example.spectraholograms.serialization.SerializationService;
import com.example.spectraholograms.service.HologramManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HologramManagerTest {
    private InMemoryRepo repo;
    private HologramManager manager;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepo();
        ConfigService config = mock(ConfigService.class);
        when(config.settings()).thenReturn(new PluginSettings(32, 0.28, 1200, false, 40, Set.of(), Set.of(), false, true, false));
        manager = new HologramManager(repo, new SerializationService(), config);
    }

    @Test
    void insertAndRemoveLineEdgeCases() {
        manager.create("a", new HologramLocation("world", 0, 64, 0, 0f, 0f), "line1");

        Hologram insertedAtEnd = manager.insertLine("a", 1, "line2");
        assertEquals(2, insertedAtEnd.lines().size());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> manager.insertLine("a", 3, "bad"));
        assertTrue(ex.getMessage().contains("range"));

        Hologram removed = manager.removeLine("a", 0);
        assertEquals(1, removed.lines().size());
    }

    @Test
    void renameCollisionThrows() {
        manager.create("first", new HologramLocation("world", 0, 64, 0, null, null), "one");
        manager.create("second", new HologramLocation("world", 0, 64, 0, null, null), "two");

        assertThrows(IllegalArgumentException.class, () -> manager.rename("first", "second"));
    }

    @Test
    void setLineInvalidIndexThrows() {
        manager.create("id", new HologramLocation("world", 0, 64, 0, null, null), "line");
        assertThrows(IllegalArgumentException.class, () -> manager.setLine("id", -1, "x"));
        assertThrows(IllegalArgumentException.class, () -> manager.setLine("id", 3, "x"));
    }

    private static class InMemoryRepo implements HologramRepository {
        private final List<Hologram> storage = new ArrayList<>();

        @Override
        public List<Hologram> loadAll() {
            return List.copyOf(storage);
        }

        @Override
        public void saveAll(Collection<Hologram> holograms) {
            storage.clear();
            storage.addAll(holograms);
        }
    }
}
