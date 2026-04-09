package com.github.cybellereaper.spectraholograms.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Locale;

public class PlaceholderService {
    private final PlaceholderApiAdapter adapter;

    public PlaceholderService(PlaceholderApiAdapter adapter) {
        this.adapter = adapter;
    }

    public String apply(Player player, Location location, String input) {
        int online = safeOnlineCount();
        String resolved = input
                .replace("{player}", player.getName())
                .replace("{world}", location.getWorld() == null ? "unknown" : location.getWorld().getName())
                .replace("{x}", String.format(Locale.US, "%.2f", location.getX()))
                .replace("{y}", String.format(Locale.US, "%.2f", location.getY()))
                .replace("{z}", String.format(Locale.US, "%.2f", location.getZ()))
                .replace("{online}", Integer.toString(online));
        if (adapter == null) {
            return resolved;
        }
        return adapter.apply(player, resolved);
    }

    private int safeOnlineCount() {
        try {
            return Bukkit.getOnlinePlayers().size();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
