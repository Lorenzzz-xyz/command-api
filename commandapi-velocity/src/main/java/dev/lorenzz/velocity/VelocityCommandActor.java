package dev.lorenzz.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.lorenzz.common.CommandActor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityCommandActor implements CommandActor {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private final CommandSource source;

    public VelocityCommandActor(CommandSource source) {
        this.source = source;
    }

    @Override
    public void sendMessage(String message) {
        source.sendMessage(LEGACY.deserialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return !(source instanceof Player);
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public String getName() {
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        }
        return "Console";
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(source)) {
            return type.cast(source);
        }
        return null;
    }

    public CommandSource getSource() {
        return source;
    }
}
