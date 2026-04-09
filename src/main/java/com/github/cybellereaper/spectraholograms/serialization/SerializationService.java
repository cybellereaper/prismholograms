package com.github.cybellereaper.spectraholograms.serialization;

import com.github.cybellereaper.spectraholograms.model.HologramLine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class SerializationService {
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public HologramLine line(String text) {
        Component component = serializer.deserialize(text);
        return new HologramLine(text, component);
    }

    public String serialize(Component component) {
        return serializer.serialize(component);
    }
}
