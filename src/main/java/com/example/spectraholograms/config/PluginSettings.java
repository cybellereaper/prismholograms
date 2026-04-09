package com.example.spectraholograms.config;

import java.util.Set;

public record PluginSettings(
        double defaultVisibilityRange,
        double lineSpacing,
        long autosaveIntervalTicks,
        boolean debugLogging,
        long placeholderRefreshIntervalTicks,
        Set<String> worldWhitelist,
        Set<String> worldBlacklist,
        boolean placeholderApiEnabled,
        boolean hiddenByDefaultAllowed,
        boolean viewCommandDefaultAllowed
) {
}
