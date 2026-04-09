package com.github.cybellereaper.spectraholograms.repository;

import com.github.cybellereaper.spectraholograms.model.*;
import com.github.cybellereaper.spectraholograms.serialization.SerializationService;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class YamlHologramRepository implements HologramRepository {
    private final File file;
    private final Logger logger;
    private final SerializationService serializationService;

    public YamlHologramRepository(File file, Logger logger, SerializationService serializationService) {
        this.file = file;
        this.logger = logger;
        this.serializationService = serializationService;
    }

    @Override
    public List<Hologram> loadAll() {
        ensureParent();
        if (!file.exists()) {
            return List.of();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("holograms");
        if (root == null) {
            return List.of();
        }

        List<Hologram> loaded = new ArrayList<>();
        for (String idKey : root.getKeys(false)) {
            try {
                ConfigurationSection sec = Objects.requireNonNull(root.getConfigurationSection(idKey), "Missing section");
                loaded.add(readHologram(idKey, sec));
            } catch (Exception ex) {
                logger.warning("Skipping malformed hologram '" + idKey + "': " + ex.getMessage());
            }
        }
        return loaded;
    }

    private Hologram readHologram(String id, ConfigurationSection sec) {
        String world = sec.getString("world");
        if (world == null || world.isBlank()) {
            throw new IllegalArgumentException("world is required");
        }

        List<String> lines = sec.getStringList("lines");
        List<HologramLine> hologramLines = new ArrayList<>();
        for (String line : lines) {
            hologramLines.add(serializationService.line(line));
        }

        ConfigurationSection styleSec = sec.getConfigurationSection("style");
        HologramStyle style = HologramStyle.defaults();
        if (styleSec != null) {
            Display.Billboard billboard = Display.Billboard.valueOf(styleSec.getString("billboard", "CENTER"));
            Color background = styleSec.contains("background") ? Color.fromARGB(styleSec.getInt("background")) : null;
            Byte opacity = styleSec.contains("textOpacity") ? (byte) styleSec.getInt("textOpacity") : null;
            style = new HologramStyle(
                    billboard,
                    styleSec.getBoolean("shadowed", true),
                    styleSec.getBoolean("seeThrough", false),
                    background,
                    opacity,
                    (float) styleSec.getDouble("scale", 1.0)
            );
        }

        ConfigurationSection viewSec = sec.getConfigurationSection("view");
        HologramViewSettings view = new HologramViewSettings(
                viewSec == null ? 32.0 : viewSec.getDouble("range", 32.0),
                viewSec != null && viewSec.getBoolean("hiddenByDefault", false)
        );

        Instant createdAt = Instant.ofEpochMilli(sec.getLong("meta.createdAt", System.currentTimeMillis()));
        Instant updatedAt = Instant.ofEpochMilli(sec.getLong("meta.updatedAt", System.currentTimeMillis()));

        return new Hologram(
                new HologramId(id),
                new HologramLocation(
                        world,
                        sec.getDouble("x"),
                        sec.getDouble("y"),
                        sec.getDouble("z"),
                        sec.contains("yaw") ? (float) sec.getDouble("yaw") : null,
                        sec.contains("pitch") ? (float) sec.getDouble("pitch") : null
                ),
                hologramLines,
                view,
                style,
                createdAt,
                updatedAt
        );
    }

    @Override
    public void saveAll(Collection<Hologram> holograms) {
        ensureParent();
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("holograms");

        for (Hologram hologram : holograms) {
            ConfigurationSection sec = root.createSection(hologram.id().value());
            sec.set("world", hologram.location().world());
            sec.set("x", hologram.location().x());
            sec.set("y", hologram.location().y());
            sec.set("z", hologram.location().z());
            sec.set("yaw", hologram.location().yaw());
            sec.set("pitch", hologram.location().pitch());
            sec.set("lines", hologram.lines().stream().map(HologramLine::rawText).toList());
            sec.set("view.range", hologram.viewSettings().visibilityRange());
            sec.set("view.hiddenByDefault", hologram.viewSettings().hiddenByDefault());
            sec.set("style.billboard", hologram.style().billboard().name());
            sec.set("style.shadowed", hologram.style().shadowed());
            sec.set("style.seeThrough", hologram.style().seeThrough());
            sec.set("style.scale", hologram.style().scale());
            if (hologram.style().backgroundColor() != null) {
                sec.set("style.background", hologram.style().backgroundColor().asARGB());
            }
            if (hologram.style().textOpacity() != null) {
                sec.set("style.textOpacity", hologram.style().textOpacity());
            }
            sec.set("meta.createdAt", hologram.createdAt().toEpochMilli());
            sec.set("meta.updatedAt", hologram.updatedAt().toEpochMilli());
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save holograms", e);
        }
    }

    private void ensureParent() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
