package com.github.cybellereaper.spectraholograms.service;

import com.github.cybellereaper.spectraholograms.config.ConfigService;
import com.github.cybellereaper.spectraholograms.model.*;
import com.github.cybellereaper.spectraholograms.repository.HologramRepository;
import com.github.cybellereaper.spectraholograms.serialization.SerializationService;

import java.time.Instant;
import java.util.*;

public class HologramManager {
    private final HologramRepository repository;
    private final SerializationService serializationService;
    private final ConfigService configService;
    private final Map<String, Hologram> holograms = new HashMap<>();

    public HologramManager(HologramRepository repository, SerializationService serializationService, ConfigService configService) {
        this.repository = repository;
        this.serializationService = serializationService;
        this.configService = configService;
    }

    public int load() {
        holograms.clear();
        for (Hologram hologram : repository.loadAll()) {
            holograms.put(hologram.id().value().toLowerCase(Locale.ROOT), hologram);
        }
        return holograms.size();
    }

    public void save() {
        repository.saveAll(holograms.values());
    }

    public Collection<Hologram> all() {
        return List.copyOf(holograms.values());
    }

    public Optional<Hologram> get(String id) {
        return Optional.ofNullable(holograms.get(id.toLowerCase(Locale.ROOT)));
    }

    public Hologram create(String id, HologramLocation location, String initialText) {
        String key = id.toLowerCase(Locale.ROOT);
        if (holograms.containsKey(key)) {
            throw new IllegalArgumentException("A hologram with that id already exists.");
        }

        Hologram created = new Hologram(
                new HologramId(id),
                location,
                List.of(serializationService.line(initialText)),
                new HologramViewSettings(configService.settings().defaultVisibilityRange(), false),
                HologramStyle.defaults(),
                Instant.now(),
                Instant.now()
        );

        holograms.put(key, created);
        save();
        return created;
    }

    public boolean delete(String id) {
        Hologram removed = holograms.remove(id.toLowerCase(Locale.ROOT));
        if (removed == null) {
            return false;
        }
        save();
        return true;
    }

    public Hologram rename(String oldId, String newId) {
        String oldKey = oldId.toLowerCase(Locale.ROOT);
        String newKey = newId.toLowerCase(Locale.ROOT);

        if (!holograms.containsKey(oldKey)) {
            throw new IllegalArgumentException("Hologram not found: " + oldId);
        }
        if (holograms.containsKey(newKey)) {
            throw new IllegalArgumentException("Cannot rename, id already exists: " + newId);
        }

        Hologram renamed = holograms.remove(oldKey).withId(new HologramId(newId));
        holograms.put(newKey, renamed);
        save();
        return renamed;
    }

    public Hologram move(String id, HologramLocation location) {
        Hologram updated = require(id).withLocation(location);
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    public Hologram setLine(String id, int index, String text) {
        Hologram hologram = require(id);
        validateIndex(index, hologram.lines().size());
        Hologram updated = hologram.setLine(index, serializationService.line(text));
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    public Hologram addLine(String id, String text) {
        Hologram updated = require(id).addLine(serializationService.line(text));
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    public Hologram insertLine(String id, int index, String text) {
        Hologram hologram = require(id);
        if (index < 0 || index > hologram.lines().size()) {
            throw new IllegalArgumentException("Line index out of range");
        }
        Hologram updated = hologram.insertLine(index, serializationService.line(text));
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    public Hologram removeLine(String id, int index) {
        Hologram hologram = require(id);
        validateIndex(index, hologram.lines().size());
        Hologram updated = hologram.removeLine(index);
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    public Hologram clearLines(String id) {
        Hologram updated = require(id).withLines(List.of());
        holograms.put(id.toLowerCase(Locale.ROOT), updated);
        save();
        return updated;
    }

    private void validateIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Line index out of range");
        }
    }

    private Hologram require(String id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("Hologram not found: " + id));
    }
}
