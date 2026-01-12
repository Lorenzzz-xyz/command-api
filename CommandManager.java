package dev.lorenzz.commandapi;

import dev.lorenzz.commandapi.exception.CommandException;
import dev.lorenzz.commandapi.provider.Provider;
import dev.lorenzz.commandapi.provider.ProviderRegistry;
import dev.lorenzz.commandapi.registration.CommandRegistrar;
import dev.lorenzz.commandapi.util.LanguageLocale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public final class CommandManager {

    private final Plugin plugin;
    private final CommandRegistrar registrar;
    private final CommandMap commandMap;

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
        this.commandMap = resolveCommandMap();
        this.registrar = new CommandRegistrar(plugin, commandMap);

        ProviderRegistry.registerDefaults();
        LanguageLocale.init(plugin);
    }

    public CommandManager register(Object commandContainer) {
        registrar.register(commandContainer);

        return this;
    }

    public CommandManager register(Object... commands) {
        for (Object command : commands) {
            register(command);
        }

        return this;
    }

    public <T> CommandManager registerProvider(Class<T> type, Provider<T> provider) {
        ProviderRegistry.register(type, provider);

        return this;
    }

    private CommandMap resolveCommandMap() {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            return (CommandMap) f.get(Bukkit.getServer());
        } catch (Exception ex) {
            throw new CommandException("Unable to access CommandMap", ex);
        }
    }
}