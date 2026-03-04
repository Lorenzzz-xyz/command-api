package dev.lorenzz.bukkit;

import dev.lorenzz.common.dispatch.CommandDispatcher;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;

public final class BukkitCommandManager {

    private final Plugin plugin;
    private final BukkitCommandPlatform platform;
    private final ProviderRegistry providerRegistry;
    private final CommandDispatcher dispatcher;
    private final PluginCommandFactory factory = new PluginCommandFactory();

    public BukkitCommandManager(Plugin plugin) {
        BukkitLanguageProvider languageProvider = new BukkitLanguageProvider(plugin);
        this.plugin = plugin;
        this.platform = new BukkitCommandPlatform(plugin, languageProvider);
        this.providerRegistry = new ProviderRegistry();
        this.providerRegistry.registerDefaults(languageProvider, platform.getColorTranslator());
        BukkitProviderDefaults.register(providerRegistry, languageProvider, platform.getColorTranslator());
        this.dispatcher = new CommandDispatcher(platform, providerRegistry);
    }

    public BukkitCommandManager register(Object commandContainer) {
        List<CommandDispatcher.RootCommand> roots = dispatcher.scan(commandContainer);
        CommandMap commandMap = platform.getCommandMap();

        for (CommandDispatcher.RootCommand root : roots) {
            org.bukkit.command.Command existing = commandMap.getCommand(root.name);
            if (existing != null) {
                existing.unregister(commandMap);
            }

            org.bukkit.command.Command bukkitCmd = factory.create(root.name, root.aliases,
                    (sender, label, args) -> {
                        BukkitCommandActor actor = new BukkitCommandActor(sender);
                        dispatcher.handleCommand(root.name, actor, label, args);
                        return true;
                    },
                    (sender, label, args) -> {
                        BukkitCommandActor actor = new BukkitCommandActor(sender);
                        return dispatcher.handleTabComplete(root.name, actor, args);
                    }
            );

            bukkitCmd.setDescription(root.description);
            bukkitCmd.setUsage(root.usage);
            commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), bukkitCmd);
        }

        return this;
    }

    public BukkitCommandManager register(Object... commands) {
        for (Object command : commands) {
            register(command);
        }
        return this;
    }

    public <T> BukkitCommandManager registerProvider(Class<T> type, Provider<T> provider) {
        providerRegistry.register(type, provider);
        return this;
    }
}
