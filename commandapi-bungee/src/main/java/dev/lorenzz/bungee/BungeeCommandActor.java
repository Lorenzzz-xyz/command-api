package dev.lorenzz.bungee;

import dev.lorenzz.common.CommandActor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeCommandActor implements CommandActor {

    private final CommandSender sender;

    public BungeeCommandActor(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return !(sender instanceof ProxiedPlayer);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
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
