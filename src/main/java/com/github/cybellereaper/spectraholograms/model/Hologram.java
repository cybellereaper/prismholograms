package com.github.cybellereaper.spectraholograms.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Hologram(
        HologramId id,
        HologramLocation location,
        List<HologramLine> lines,
        HologramViewSettings viewSettings,
        HologramStyle style,
        Instant createdAt,
        Instant updatedAt
) {
    public Hologram {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(lines, "lines");
        Objects.requireNonNull(viewSettings, "viewSettings");
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        lines = List.copyOf(lines);
    }

    public Hologram withLines(List<HologramLine> newLines) {
        return new Hologram(id, location, List.copyOf(newLines), viewSettings, style, createdAt, Instant.now());
    }

    public Hologram withLocation(HologramLocation newLocation) {
        return new Hologram(id, newLocation, lines, viewSettings, style, createdAt, Instant.now());
    }

    public Hologram withId(HologramId newId) {
        return new Hologram(newId, location, lines, viewSettings, style, createdAt, Instant.now());
    }

    public Hologram withViewSettings(HologramViewSettings settings) {
        return new Hologram(id, location, lines, settings, style, createdAt, Instant.now());
    }

    public Hologram withStyle(HologramStyle newStyle) {
        return new Hologram(id, location, lines, viewSettings, newStyle, createdAt, Instant.now());
    }

    public Hologram addLine(HologramLine line) {
        List<HologramLine> mutated = new ArrayList<>(lines);
        mutated.add(line);
        return withLines(mutated);
    }

    public Hologram insertLine(int index, HologramLine line) {
        List<HologramLine> mutated = new ArrayList<>(lines);
        mutated.add(index, line);
        return withLines(mutated);
    }

    public Hologram setLine(int index, HologramLine line) {
        List<HologramLine> mutated = new ArrayList<>(lines);
        mutated.set(index, line);
        return withLines(mutated);
    }

    public Hologram removeLine(int index) {
        List<HologramLine> mutated = new ArrayList<>(lines);
        mutated.remove(index);
        return withLines(mutated);
    }
}
