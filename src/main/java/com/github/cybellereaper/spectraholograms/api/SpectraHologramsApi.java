package com.github.cybellereaper.spectraholograms.api;

import com.github.cybellereaper.spectraholograms.model.Hologram;
import com.github.cybellereaper.spectraholograms.model.HologramLocation;

import java.util.Collection;
import java.util.Optional;

/**
 * Public API for interacting with SpectraHolograms from other plugins.
 */
public interface SpectraHologramsApi {
    Collection<Hologram> getHolograms();

    Optional<Hologram> getHologram(String id);

    Hologram createHologram(String id, HologramLocation location, String initialText);

    boolean deleteHologram(String id);

    Hologram renameHologram(String oldId, String newId);

    Hologram moveHologram(String id, HologramLocation location);

    Hologram setLine(String id, int index, String text);

    Hologram addLine(String id, String text);

    Hologram insertLine(String id, int index, String text);

    Hologram removeLine(String id, int index);

    Hologram clearLines(String id);
}
