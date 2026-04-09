package com.github.cybellereaper.spectraholograms.api;

import com.github.cybellereaper.spectraholograms.model.Hologram;
import com.github.cybellereaper.spectraholograms.model.HologramLocation;
import com.github.cybellereaper.spectraholograms.service.HologramManager;
import com.github.cybellereaper.spectraholograms.service.HologramSpawner;

import java.util.Collection;
import java.util.Optional;

/**
 * Default API implementation that coordinates persistent state and spawned entities.
 */
public class DefaultSpectraHologramsApi implements SpectraHologramsApi {
    private final HologramManager hologramManager;
    private final HologramSpawner hologramSpawner;

    public DefaultSpectraHologramsApi(HologramManager hologramManager, HologramSpawner hologramSpawner) {
        this.hologramManager = hologramManager;
        this.hologramSpawner = hologramSpawner;
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return hologramManager.all();
    }

    @Override
    public Optional<Hologram> getHologram(String id) {
        return hologramManager.get(id);
    }

    @Override
    public Hologram createHologram(String id, HologramLocation location, String initialText) {
        Hologram created = hologramManager.create(id, location, initialText);
        hologramSpawner.spawnOrRebuild(created);
        return created;
    }

    @Override
    public boolean deleteHologram(String id) {
        boolean deleted = hologramManager.delete(id);
        if (!deleted) {
            return false;
        }

        hologramSpawner.despawn(id);
        return true;
    }

    @Override
    public Hologram renameHologram(String oldId, String newId) {
        Hologram renamed = hologramManager.rename(oldId, newId);
        hologramSpawner.despawn(oldId);
        hologramSpawner.spawnOrRebuild(renamed);
        return renamed;
    }

    @Override
    public Hologram moveHologram(String id, HologramLocation location) {
        Hologram moved = hologramManager.move(id, location);
        hologramSpawner.spawnOrRebuild(moved);
        return moved;
    }

    @Override
    public Hologram setLine(String id, int index, String text) {
        Hologram updated = hologramManager.setLine(id, index, text);
        hologramSpawner.spawnOrRebuild(updated);
        return updated;
    }

    @Override
    public Hologram addLine(String id, String text) {
        Hologram updated = hologramManager.addLine(id, text);
        hologramSpawner.spawnOrRebuild(updated);
        return updated;
    }

    @Override
    public Hologram insertLine(String id, int index, String text) {
        Hologram updated = hologramManager.insertLine(id, index, text);
        hologramSpawner.spawnOrRebuild(updated);
        return updated;
    }

    @Override
    public Hologram removeLine(String id, int index) {
        Hologram updated = hologramManager.removeLine(id, index);
        hologramSpawner.spawnOrRebuild(updated);
        return updated;
    }

    @Override
    public Hologram clearLines(String id) {
        Hologram updated = hologramManager.clearLines(id);
        hologramSpawner.spawnOrRebuild(updated);
        return updated;
    }
}
