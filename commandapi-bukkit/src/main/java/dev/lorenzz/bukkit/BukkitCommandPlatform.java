package dev.lorenzz.bukkit;

import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.CommandPlatform;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.exception.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.stream.Collectors;

public final class BukkitCommandPlatform implements CommandPlatform {

    private final Plugin plugin;
    private final CommandMap commandMap;
    private final ColorTranslator colorTranslator;
    private final LanguageProvider languageProvider;

    private static volatile Field commandMapField;

    public BukkitCommandPlatform(Plugin plugin, LanguageProvider languageProvider) {
        this.plugin = plugin;
        this.commandMap = resolveCommandMap();
        this.colorTranslator = new BukkitColorTranslator();
        this.languageProvider = languageProvider;
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public ColorTranslator getColorTranslator() {
        return colorTranslator;
    }

    @Override
    public LanguageProvider getLanguageProvider() {
        return languageProvider;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    private static CommandMap resolveCommandMap() {
        try {
            if (commandMapField == null) {
                synchronized (BukkitCommandPlatform.class) {
                    if (commandMapField == null) {
                        commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                        commandMapField.setAccessible(true);
                    }
                }
            }
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception ex) {
            throw new CommandException("Unable to access CommandMap", ex);
        }
    }
}
