package com.github.cybellereaper.spectraholograms.service;

import com.github.cybellereaper.spectraholograms.model.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class HologramVisibilityService {
    private final Plugin plugin;
    private final HologramSpawner spawner;
    private final Map<UUID, Set<String>> hiddenByPlayer = new HashMap<>();

    public HologramVisibilityService(Plugin plugin, HologramSpawner spawner) {
        this.plugin = plugin;
        this.spawner = spawner;
    }

    public void hideFor(String hologramId, Player player) {
        hiddenByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(hologramId.toLowerCase(Locale.ROOT));
        applyVisibility(player, hologramId, false);
    }

    public void showFor(String hologramId, Player player) {
        hiddenByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).remove(hologramId.toLowerCase(Locale.ROOT));
        applyVisibility(player, hologramId, true);
    }

    public boolean isHidden(String hologramId, Player player) {
        return hiddenByPlayer.getOrDefault(player.getUniqueId(), Set.of()).contains(hologramId.toLowerCase(Locale.ROOT));
    }

    public void clearPlayer(UUID playerId) {
        hiddenByPlayer.remove(playerId);
    }

    public void refreshPlayer(Player player, Collection<Hologram> holograms) {
        Location playerLocation = player.getLocation();
        for (Hologram hologram : holograms) {
            List<UUID> ids = spawner.getEntityIds(hologram.id().value());
            boolean inSameWorld = playerLocation.getWorld() != null
                    && playerLocation.getWorld().getName().equals(hologram.location().world());
            boolean inRange = inSameWorld && playerLocation.distanceSquared(new Location(
                    playerLocation.getWorld(),
                    hologram.location().x(),
                    hologram.location().y(),
                    hologram.location().z()
            )) <= (hologram.viewSettings().visibilityRange() * hologram.viewSettings().visibilityRange());

            boolean shouldShow = inRange && !isHidden(hologram.id().value(), player);
            for (UUID entityId : ids) {
                Entity entity = plugin.getServer().getEntity(entityId);
                if (entity == null) {
                    continue;
                }
                if (shouldShow) {
                    player.showEntity(plugin, entity);
                } else {
                    player.hideEntity(plugin, entity);
                }
            }
        }
    }

    private void applyVisibility(Player player, String hologramId, boolean visible) {
        for (UUID id : spawner.getEntityIds(hologramId)) {
            Entity entity = plugin.getServer().getEntity(id);
            if (entity == null) {
                continue;
            }
            if (visible) {
                player.showEntity(plugin, entity);
            } else {
                player.hideEntity(plugin, entity);
            }
        }
    }
}
