package com.github.cybellereaper.spectraholograms.repository;

import com.github.cybellereaper.spectraholograms.model.Hologram;

import java.util.Collection;
import java.util.List;

public interface HologramRepository {
    List<Hologram> loadAll();

    void saveAll(Collection<Hologram> holograms);
}
