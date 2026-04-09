package com.example.spectraholograms.repository;

import com.example.spectraholograms.model.Hologram;

import java.util.Collection;
import java.util.List;

public interface HologramRepository {
    List<Hologram> loadAll();

    void saveAll(Collection<Hologram> holograms);
}
