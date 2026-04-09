package com.github.cybellereaper.spectraholograms.model;

import java.util.Objects;

public record HologramId(String value) {
    public HologramId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Hologram id cannot be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
