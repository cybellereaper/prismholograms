package com.github.cybellereaper.spectraholograms;

import com.github.cybellereaper.spectraholograms.config.ConfigService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ConfigServiceTest {
    @Test
    void invalidNumericInputIsClamped() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);

        when(plugin.getConfig()).thenReturn(config);
        when(config.getDouble("default-visibility-range", 32.0)).thenReturn(-10.0);
        when(config.getDouble("line-spacing", 0.28)).thenReturn(0.0);
        when(config.getLong("autosave-interval-ticks", 20L * 60L * 5L)).thenReturn(1L);
        when(config.getLong("placeholder-refresh-interval-ticks", 40L)).thenReturn(1L);

        ConfigService service = new ConfigService(plugin);
        service.load();

        assertEquals(1.0, service.settings().defaultVisibilityRange());
        assertEquals(0.01, service.settings().lineSpacing());
        assertEquals(20L, service.settings().autosaveIntervalTicks());
        assertEquals(20L, service.settings().placeholderRefreshIntervalTicks());
    }
}
