package com.example.spectraholograms.command;

import com.example.spectraholograms.SpectraHologramsPlugin;
import com.example.spectraholograms.model.Hologram;
import com.example.spectraholograms.model.HologramLocation;
import com.example.spectraholograms.service.HologramManager;
import com.example.spectraholograms.service.HologramSpawner;
import com.example.spectraholograms.service.HologramVisibilityService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class HologramCommandRegistrar implements CommandExecutor, TabCompleter {
    private final SpectraHologramsPlugin plugin;
    private final HologramManager manager;
    private final HologramSpawner spawner;
    private final HologramVisibilityService visibilityService;

    public HologramCommandRegistrar(
            SpectraHologramsPlugin plugin,
            HologramManager manager,
            HologramSpawner spawner,
            HologramVisibilityService visibilityService
    ) {
        this.plugin = plugin;
        this.manager = manager;
        this.spawner = spawner;
        this.visibilityService = visibilityService;
    }

    public void register() {
        PluginCommand command = Objects.requireNonNull(plugin.getCommand("hologram"));
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /holo <subcommand>"));
            return true;
        }

        try {
            String sub = args[0].toLowerCase(Locale.ROOT);
            return switch (sub) {
                case "create" -> create(sender, args);
                case "delete" -> delete(sender, args);
                case "list" -> list(sender);
                case "info" -> info(sender, args);
                case "tp" -> tp(sender, args);
                case "movehere" -> movehere(sender, args);
                case "setline" -> setline(sender, args);
                case "addline" -> addline(sender, args);
                case "insertline" -> insertline(sender, args);
                case "removeline" -> removeline(sender, args);
                case "clearlines" -> clearlines(sender, args);
                case "rename" -> rename(sender, args);
                case "hide" -> hide(sender, args);
                case "show" -> show(sender, args);
                case "reload" -> reload(sender);
                default -> {
                    sender.sendMessage(Component.text("Unknown subcommand."));
                    yield true;
                }
            };
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Error: " + ex.getMessage()));
            return true;
        } catch (Exception ex) {
            sender.sendMessage(Component.text("Unexpected error."));
            plugin.getLogger().warning("Command failed: " + ex.getMessage());
            return true;
        }
    }

    private boolean create(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.create");
        Player player = requirePlayer(sender);
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo create <id> [text]");
        }
        String text = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "New hologram";
        Hologram hologram = manager.create(args[1], toLocation(player.getLocation()), text);
        spawner.spawnOrRebuild(hologram);
        sender.sendMessage(Component.text("Created hologram '" + hologram.id() + "'."));
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.delete");
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo delete <id>");
        }
        spawner.despawn(args[1]);
        if (!manager.delete(args[1])) {
            throw new IllegalArgumentException("Hologram not found.");
        }
        sender.sendMessage(Component.text("Deleted hologram."));
        return true;
    }

    private boolean list(CommandSender sender) {
        requirePermission(sender, "spectraholograms.view");
        sender.sendMessage(Component.text("Holograms (" + manager.all().size() + "):"));
        for (Hologram hologram : manager.all()) {
            sender.sendMessage(Component.text(" - " + hologram.id().value()));
        }
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.view");
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo info <id>");
        }
        Hologram hologram = manager.get(args[1]).orElseThrow(() -> new IllegalArgumentException("Not found."));
        sender.sendMessage(Component.text("Id: " + hologram.id()));
        sender.sendMessage(Component.text("World: " + hologram.location().world()));
        sender.sendMessage(Component.text("Pos: " + hologram.location().x() + ", " + hologram.location().y() + ", " + hologram.location().z()));
        sender.sendMessage(Component.text("Lines: " + hologram.lines().size()));
        return true;
    }

    private boolean tp(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.teleport");
        Player player = requirePlayer(sender);
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo tp <id>");
        }
        Hologram hologram = manager.get(args[1]).orElseThrow(() -> new IllegalArgumentException("Not found."));
        hologram.location().resolveWorld().ifPresentOrElse(world -> {
            player.teleport(new org.bukkit.Location(world, hologram.location().x(), hologram.location().y(), hologram.location().z()));
            sender.sendMessage(Component.text("Teleported."));
        }, () -> sender.sendMessage(Component.text("World is not loaded.")));
        return true;
    }

    private boolean movehere(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        Player player = requirePlayer(sender);
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo movehere <id>");
        }
        Hologram moved = manager.move(args[1], toLocation(player.getLocation()));
        spawner.spawnOrRebuild(moved);
        sender.sendMessage(Component.text("Moved hologram."));
        return true;
    }

    private boolean setline(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: /holo setline <id> <line> <text>");
        }
        int index = Integer.parseInt(args[2]) - 1;
        Hologram updated = manager.setLine(args[1], index, String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Updated line."));
        return true;
    }

    private boolean addline(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: /holo addline <id> <text>");
        }
        Hologram updated = manager.addLine(args[1], String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Added line."));
        return true;
    }

    private boolean insertline(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: /holo insertline <id> <line> <text>");
        }
        int index = Integer.parseInt(args[2]) - 1;
        Hologram updated = manager.insertLine(args[1], index, String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Inserted line."));
        return true;
    }

    private boolean removeline(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: /holo removeline <id> <line>");
        }
        int index = Integer.parseInt(args[2]) - 1;
        Hologram updated = manager.removeLine(args[1], index);
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Removed line."));
        return true;
    }

    private boolean clearlines(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo clearlines <id>");
        }
        Hologram updated = manager.clearLines(args[1]);
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Cleared lines."));
        return true;
    }

    private boolean rename(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.edit");
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: /holo rename <oldId> <newId>");
        }
        Hologram updated = manager.rename(args[1], args[2]);
        spawner.despawn(args[1]);
        spawner.spawnOrRebuild(updated);
        sender.sendMessage(Component.text("Renamed hologram."));
        return true;
    }

    private boolean hide(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.view");
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo hide <id> [player]");
        }
        Player target = targetPlayerOrSelf(sender, args, 2);
        visibilityService.hideFor(args[1], target);
        sender.sendMessage(Component.text("Hologram hidden for " + target.getName()));
        return true;
    }

    private boolean show(CommandSender sender, String[] args) {
        requirePermission(sender, "spectraholograms.view");
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: /holo show <id> [player]");
        }
        Player target = targetPlayerOrSelf(sender, args, 2);
        visibilityService.showFor(args[1], target);
        sender.sendMessage(Component.text("Hologram shown for " + target.getName()));
        return true;
    }

    private boolean reload(CommandSender sender) {
        requirePermission(sender, "spectraholograms.reload");
        plugin.reloadAll();
        sender.sendMessage(Component.text("SpectraHolograms reloaded."));
        return true;
    }

    private Player targetPlayerOrSelf(CommandSender sender, String[] args, int idx) {
        if (args.length > idx) {
            Player target = Bukkit.getPlayerExact(args[idx]);
            if (target == null) {
                throw new IllegalArgumentException("Target player not found.");
            }
            return target;
        }
        return requirePlayer(sender);
    }

    private void requirePermission(CommandSender sender, String permission) {
        if (sender.hasPermission("spectraholograms.admin") || sender.hasPermission(permission)) {
            return;
        }
        throw new IllegalArgumentException("You do not have permission: " + permission);
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        throw new IllegalArgumentException("This command can only be used by a player.");
    }

    private HologramLocation toLocation(org.bukkit.Location location) {
        return new HologramLocation(
                Objects.requireNonNull(location.getWorld()).getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("create", "delete", "list", "info", "tp", "movehere", "setline", "addline", "insertline",
                    "removeline", "clearlines", "rename", "hide", "show", "reload"), args[0]);
        }
        if (args.length == 2 && !List.of("list", "reload", "create").contains(args[0].toLowerCase(Locale.ROOT))) {
            return filter(manager.all().stream().map(h -> h.id().value()).sorted().toList(), args[1]);
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("hide") || args[0].equalsIgnoreCase("show"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
