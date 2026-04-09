package com.example.spectraholograms.listener;

import com.example.spectraholograms.service.HologramManager;
import com.example.spectraholograms.service.HologramSpawner;
import com.example.spectraholograms.service.HologramVisibilityService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramPlayerListener implements Listener {
    private final HologramManager manager;
    private final HologramSpawner spawner;
    private final HologramVisibilityService visibilityService;
    private final long throttleMs;
    private final Map<UUID, Long> lastRefresh = new HashMap<>();

    public HologramPlayerListener(HologramManager manager, HologramSpawner spawner, HologramVisibilityService visibilityService, long throttleMs) {
        this.manager = manager;
        this.spawner = spawner;
        this.visibilityService = visibilityService;
        this.throttleMs = throttleMs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refresh(event.getPlayer(), true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        visibilityService.clearPlayer(event.getPlayer().getUniqueId());
        lastRefresh.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        refresh(event.getPlayer(), true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        refresh(event.getPlayer(), false);
    }

    private void refresh(Player player, boolean force) {
        long now = System.currentTimeMillis();
        long previous = lastRefresh.getOrDefault(player.getUniqueId(), 0L);
        if (!force && now - previous < throttleMs) {
            return;
        }
        lastRefresh.put(player.getUniqueId(), now);
        visibilityService.refreshPlayer(player, manager.all());
        spawner.updatePlayerText(player, manager.all());
    }
}
