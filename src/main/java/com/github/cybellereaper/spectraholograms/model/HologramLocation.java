package com.github.cybellereaper.spectraholograms.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public record HologramLocation(String world, double x, double y, double z, Float yaw, Float pitch) {
    public Optional<World> resolveWorld() {
        return Optional.ofNullable(Bukkit.getWorld(world));
    }

    public Location toBukkitLocation() {
        float resolvedYaw = yaw == null ? 0f : yaw;
        float resolvedPitch = pitch == null ? 0f : pitch;
        return new Location(Bukkit.getWorld(world), x, y, z, resolvedYaw, resolvedPitch);
    }

    public HologramLocation withPosition(double nx, double ny, double nz, float newYaw, float newPitch) {
        return new HologramLocation(world, nx, ny, nz, newYaw, newPitch);
    }
}
