package com.example.spectraholograms;

import com.example.spectraholograms.command.HologramCommandRegistrar;
import com.example.spectraholograms.config.ConfigService;
import com.example.spectraholograms.listener.HologramPlayerListener;
import com.example.spectraholograms.listener.HologramWorldListener;
import com.example.spectraholograms.placeholder.PlaceholderApiAdapter;
import com.example.spectraholograms.placeholder.PlaceholderService;
import com.example.spectraholograms.placeholder.ReflectionPlaceholderApiAdapter;
import com.example.spectraholograms.repository.HologramRepository;
import com.example.spectraholograms.repository.YamlHologramRepository;
import com.example.spectraholograms.serialization.SerializationService;
import com.example.spectraholograms.service.HologramManager;
import com.example.spectraholograms.service.HologramSpawner;
import com.example.spectraholograms.service.HologramVisibilityService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SpectraHologramsPlugin extends JavaPlugin {
    private ConfigService configService;
    private HologramManager hologramManager;
    private HologramSpawner hologramSpawner;

    @Override
    public void onEnable() {
        this.configService = new ConfigService(this);
        this.configService.load();

        SerializationService serializationService = new SerializationService();
        HologramRepository repository = new YamlHologramRepository(new File(getDataFolder(), "holograms.yml"), getLogger(), serializationService);

        PlaceholderApiAdapter adapter = null;
        if (configService.settings().placeholderApiEnabled()) {
            adapter = ReflectionPlaceholderApiAdapter.tryCreate();
        }
        PlaceholderService placeholderService = new PlaceholderService(adapter);

        this.hologramManager = new HologramManager(repository, serializationService, configService);
        this.hologramSpawner = new HologramSpawner(this, placeholderService, configService);
        HologramVisibilityService visibilityService = new HologramVisibilityService(this, hologramSpawner);

        int loaded = hologramManager.load();
        hologramManager.all().forEach(hologramSpawner::spawnOrRebuild);

        Bukkit.getPluginManager().registerEvents(new HologramPlayerListener(
                hologramManager,
                hologramSpawner,
                visibilityService,
                200L
        ), this);
        Bukkit.getPluginManager().registerEvents(new HologramWorldListener(hologramManager, hologramSpawner), this);

        new HologramCommandRegistrar(this, hologramManager, hologramSpawner, visibilityService).register();

        long autosave = configService.settings().autosaveIntervalTicks();
        getServer().getScheduler().runTaskTimerAsynchronously(this, hologramManager::save, autosave, autosave);

        long refreshTicks = configService.settings().placeholderRefreshIntervalTicks();
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                hologramSpawner.updatePlayerText(player, hologramManager.all());
                visibilityService.refreshPlayer(player, hologramManager.all());
            }
        }, refreshTicks, refreshTicks);

        getLogger().info("Loaded " + loaded + " holograms.");
        getLogger().info("PlaceholderAPI integration: " + (adapter != null));
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.save();
        }
        if (hologramSpawner != null) {
            hologramSpawner.despawnAll();
        }
    }

    public void reloadAll() {
        configService.load();
        hologramSpawner.despawnAll();
        int loaded = hologramManager.load();
        hologramManager.all().forEach(hologramSpawner::spawnOrRebuild);
        getLogger().info("Reloaded SpectraHolograms. Holograms: " + loaded);
    }
}
