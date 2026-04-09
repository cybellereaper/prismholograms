package com.example.spectraholograms.model;

import net.kyori.adventure.text.Component;

import java.util.Objects;

public record HologramLine(String rawText, Component component) {
    public HologramLine {
        Objects.requireNonNull(rawText, "rawText");
        Objects.requireNonNull(component, "component");
    }
}
