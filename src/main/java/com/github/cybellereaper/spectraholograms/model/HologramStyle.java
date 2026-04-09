package com.github.cybellereaper.spectraholograms.model;

import org.bukkit.Color;
import org.bukkit.entity.Display;

public record HologramStyle(
        Display.Billboard billboard,
        boolean shadowed,
        boolean seeThrough,
        Color backgroundColor,
        Byte textOpacity,
        float scale
) {
    public static HologramStyle defaults() {
        return new HologramStyle(Display.Billboard.CENTER, true, false, null, null, 1.0f);
    }
}
