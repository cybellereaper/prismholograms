package com.example.spectraholograms.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class ConfigService {
    private final JavaPlugin plugin;
    private PluginSettings settings;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        settings = new PluginSettings(
                Math.max(1.0, config.getDouble("default-visibility-range", 32.0)),
                Math.max(0.01, config.getDouble("line-spacing", 0.28)),
                Math.max(20L, config.getLong("autosave-interval-ticks", 20L * 60L * 5L)),
                config.getBoolean("debug-logging", false),
                Math.max(20L, config.getLong("placeholder-refresh-interval-ticks", 40L)),
                new HashSet<>(config.getStringList("world-whitelist") == null ? java.util.List.of() : config.getStringList("world-whitelist")),
                new HashSet<>(config.getStringList("world-blacklist") == null ? java.util.List.of() : config.getStringList("world-blacklist")),
                config.getBoolean("placeholderapi.enabled", true),
                config.getBoolean("allow-hidden-by-default-holograms", true),
                config.getBoolean("permissions.view-default-allowed", false)
        );
    }

    public PluginSettings settings() {
        if (settings == null) {
            throw new IllegalStateException("Config has not been loaded yet");
        }
        return settings;
    }

    public boolean isWorldAllowed(String worldName) {
        PluginSettings s = settings();
        if (!s.worldWhitelist().isEmpty() && !s.worldWhitelist().contains(worldName)) {
            return false;
        }
        return !s.worldBlacklist().contains(worldName);
    }
}
