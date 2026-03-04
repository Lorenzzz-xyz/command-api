package dev.lorenzz.bukkit;

import dev.lorenzz.common.CommandActor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class BukkitCommandActor implements CommandActor {

    private final CommandSender sender;

    public BukkitCommandActor(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(sender)) {
            return type.cast(sender);
        }
        return null;
    }

    public CommandSender getSender() {
        return sender;
    }
}
