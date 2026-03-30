package dev.lorenzz.bukkit;

import dev.lorenzz.common.ColorTranslator;
import org.bukkit.ChatColor;

public final class BukkitColorTranslator implements ColorTranslator {

    @Override
    public String translate(String input) {
        if (input == null) return "";
        input = ColorTranslator.translateHexCodes(input);
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
