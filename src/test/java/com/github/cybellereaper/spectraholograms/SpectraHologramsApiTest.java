package com.github.cybellereaper.spectraholograms;

import com.github.cybellereaper.spectraholograms.api.DefaultSpectraHologramsApi;
import com.github.cybellereaper.spectraholograms.model.Hologram;
import com.github.cybellereaper.spectraholograms.model.HologramId;
import com.github.cybellereaper.spectraholograms.model.HologramLine;
import com.github.cybellereaper.spectraholograms.model.HologramLocation;
import com.github.cybellereaper.spectraholograms.model.HologramStyle;
import com.github.cybellereaper.spectraholograms.model.HologramViewSettings;
import net.kyori.adventure.text.Component;
import com.github.cybellereaper.spectraholograms.service.HologramManager;
import com.github.cybellereaper.spectraholograms.service.HologramSpawner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SpectraHologramsApiTest {
    private HologramManager manager;
    private HologramSpawner spawner;
    private DefaultSpectraHologramsApi api;

    @BeforeEach
    void setUp() {
        manager = mock(HologramManager.class);
        spawner = mock(HologramSpawner.class);
        api = new DefaultSpectraHologramsApi(manager, spawner);
    }

    @Test
    void createHologramSpawnsEntities() {
        Hologram created = hologram("welcome");
        when(manager.create(eq("welcome"), any(), eq("hello"))).thenReturn(created);

        HologramLocation location = new HologramLocation("world", 0, 64, 0, 0f, 0f);
        Hologram result = api.createHologram("welcome", location, "hello");

        assertSame(created, result);
        verify(spawner).spawnOrRebuild(created);
    }

    @Test
    void deleteHologramDespawnOnlyWhenDeleted() {
        when(manager.delete("missing")).thenReturn(false);
        when(manager.delete("existing")).thenReturn(true);

        assertFalse(api.deleteHologram("missing"));
        assertTrue(api.deleteHologram("existing"));

        verify(spawner, never()).despawn("missing");
        verify(spawner).despawn("existing");
    }

    @Test
    void renameHologramDespawnsOldIdAndSpawnsNewVersion() {
        Hologram renamed = hologram("new-id");
        when(manager.rename("old-id", "new-id")).thenReturn(renamed);

        Hologram result = api.renameHologram("old-id", "new-id");

        assertSame(renamed, result);
        verify(spawner).despawn("old-id");
        verify(spawner).spawnOrRebuild(renamed);
    }

    private static Hologram hologram(String id) {
        return new Hologram(
                new HologramId(id),
                new HologramLocation("world", 0, 64, 0, null, null),
                List.of(new HologramLine("text", Component.text("text"))),
                new HologramViewSettings(32, false),
                HologramStyle.defaults(),
                Instant.now(),
                Instant.now()
        );
    }
}
