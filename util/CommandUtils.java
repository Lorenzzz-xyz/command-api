package dev.lorenzz.commandapi.util;

import org.bukkit.ChatColor;

public final class CommandUtils {
    private CommandUtils() {
    }

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}