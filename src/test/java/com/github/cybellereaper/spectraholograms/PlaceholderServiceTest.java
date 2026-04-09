package com.github.cybellereaper.spectraholograms;

import com.github.cybellereaper.spectraholograms.placeholder.PlaceholderService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PlaceholderServiceTest {
    @Test
    void fallbackWithoutAdapterStillResolvesBuiltins() {
        Player player = mock(Player.class);
        World world = mock(World.class);
        when(player.getName()).thenReturn("Alice");
        when(world.getName()).thenReturn("world");

        PlaceholderService service = new PlaceholderService(null);
        String result = service.apply(player, new Location(world, 12.34, 70.0, -5.5), "{player} {world} {x} {y} {z} {online}");

        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("world"));
        assertTrue(result.contains("12.34"));
    }
}
