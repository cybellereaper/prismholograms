package com.example.spectraholograms.service;

import com.example.spectraholograms.config.ConfigService;
import com.example.spectraholograms.model.Hologram;
import com.example.spectraholograms.model.HologramLine;
import com.example.spectraholograms.placeholder.PlaceholderService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class HologramSpawner {
    private final Plugin plugin;
    private final PlaceholderService placeholderService;
    private final ConfigService configService;
    private final Map<String, List<UUID>> lineEntitiesById = new HashMap<>();

    public HologramSpawner(Plugin plugin, PlaceholderService placeholderService, ConfigService configService) {
        this.plugin = plugin;
        this.placeholderService = placeholderService;
        this.configService = configService;
    }

    public void despawn(String hologramId) {
        List<UUID> uuids = lineEntitiesById.remove(hologramId.toLowerCase(Locale.ROOT));
        if (uuids == null) {
            return;
        }
        for (UUID uuid : uuids) {
            var entity = plugin.getServer().getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public void despawnAll() {
        for (String id : new ArrayList<>(lineEntitiesById.keySet())) {
            despawn(id);
        }
    }

    public void spawnOrRebuild(Hologram hologram) {
        String key = hologram.id().value().toLowerCase(Locale.ROOT);
        despawn(key);

        World world = plugin.getServer().getWorld(hologram.location().world());
        if (world == null || !configService.isWorldAllowed(world.getName())) {
            return;
        }

        List<UUID> uuids = new ArrayList<>();
        double spacing = configService.settings().lineSpacing();
        List<HologramLine> lines = hologram.lines();

        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            double lineY = hologram.location().y() - (i * spacing);
            Location location = new Location(world, hologram.location().x(), lineY, hologram.location().z(),
                    hologram.location().yaw() == null ? 0 : hologram.location().yaw(),
                    hologram.location().pitch() == null ? 0 : hologram.location().pitch());

            TextDisplay textDisplay = world.spawn(location, TextDisplay.class, display -> {
                display.setPersistent(false);
                display.setInvulnerable(true);
                display.setGravity(false);
                display.setShadowed(hologram.style().shadowed());
                display.setSeeThrough(hologram.style().seeThrough());
                display.setBillboard(hologram.style().billboard());
                if (hologram.style().backgroundColor() != null) {
                    display.setBackgroundColor(hologram.style().backgroundColor());
                }
                if (hologram.style().textOpacity() != null) {
                    display.setTextOpacity(hologram.style().textOpacity());
                }
                if (hologram.style().scale() > 0f && hologram.style().scale() != 1.0f) {
                    display.setTransformation(new Transformation(
                            new Vector3f(),
                            new Quaternionf(),
                            new Vector3f(hologram.style().scale()),
                            new Quaternionf()
                    ));
                }
                display.setText(line.rawText());
                display.addScoreboardTag("spectraholograms");
                display.addScoreboardTag("spectraholograms-id:" + hologram.id());
            });
            uuids.add(textDisplay.getUniqueId());
        }

        lineEntitiesById.put(key, uuids);
    }

    public void updatePlayerText(Player player, Collection<Hologram> holograms) {
        for (Hologram hologram : holograms) {
            List<UUID> uuids = lineEntitiesById.get(hologram.id().value().toLowerCase(Locale.ROOT));
            if (uuids == null) {
                continue;
            }
            int index = 0;
            for (UUID uuid : uuids) {
                var entity = plugin.getServer().getEntity(uuid);
                if (!(entity instanceof TextDisplay textDisplay)) {
                    continue;
                }
                if (index >= hologram.lines().size()) {
                    break;
                }
                String parsed = placeholderService.apply(player, textDisplay.getLocation(), hologram.lines().get(index).rawText());
                textDisplay.setText(parsed);
                index++;
            }
        }
    }

    public List<UUID> getEntityIds(String hologramId) {
        return List.copyOf(lineEntitiesById.getOrDefault(hologramId.toLowerCase(Locale.ROOT), List.of()));
    }
}
