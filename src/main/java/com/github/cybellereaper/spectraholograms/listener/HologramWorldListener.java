package com.github.cybellereaper.spectraholograms.listener;

import com.github.cybellereaper.spectraholograms.service.HologramManager;
import com.github.cybellereaper.spectraholograms.service.HologramSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class HologramWorldListener implements Listener {
    private final HologramManager manager;
    private final HologramSpawner spawner;

    public HologramWorldListener(HologramManager manager, HologramSpawner spawner) {
        this.manager = manager;
        this.spawner = spawner;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        manager.all().stream()
                .filter(h -> h.location().world().equals(event.getWorld().getName()))
                .forEach(spawner::spawnOrRebuild);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        manager.all().stream()
                .filter(h -> h.location().world().equals(event.getWorld().getName()))
                .filter(h -> event.getChunk().isLoaded())
                .forEach(spawner::spawnOrRebuild);
    }
}
