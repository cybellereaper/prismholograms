package com.github.cybellereaper.spectraholograms.placeholder;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class ReflectionPlaceholderApiAdapter implements PlaceholderApiAdapter {
    private final Method method;

    public ReflectionPlaceholderApiAdapter(Method method) {
        this.method = method;
    }

    @Override
    public String apply(Player player, String input) {
        try {
            return (String) method.invoke(null, player, input);
        } catch (Exception ignored) {
            return input;
        }
    }

    public static PlaceholderApiAdapter tryCreate() {
        try {
            Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = clazz.getMethod("setPlaceholders", Player.class, String.class);
            return new ReflectionPlaceholderApiAdapter(method);
        } catch (Exception ignored) {
            return null;
        }
    }
}
