package dev.lorenzz.bungee;

import dev.lorenzz.common.ColorTranslator;
import net.md_5.bungee.api.ChatColor;

public final class BungeeColorTranslator implements ColorTranslator {

    @Override
    public String translate(String input) {
        if (input == null) return "";
        input = ColorTranslator.translateHexCodes(input);
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
