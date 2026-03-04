package dev.lorenzz.bungee;

import dev.lorenzz.common.dispatch.CommandDispatcher;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;

public final class BungeeCommandManager {

    private final Plugin plugin;
    private final BungeeCommandPlatform platform;
    private final ProviderRegistry providerRegistry;
    private final CommandDispatcher dispatcher;

    public BungeeCommandManager(Plugin plugin) {
        BungeeLanguageProvider languageProvider = new BungeeLanguageProvider();
        this.plugin = plugin;
        this.platform = new BungeeCommandPlatform(plugin, languageProvider);
        this.providerRegistry = new ProviderRegistry();
        this.providerRegistry.registerDefaults(languageProvider, platform.getColorTranslator());
        BungeeProviderDefaults.register(providerRegistry, languageProvider, platform.getColorTranslator());
        this.dispatcher = new CommandDispatcher(platform, providerRegistry);
    }

    public BungeeCommandManager register(Object commandContainer) {
        List<CommandDispatcher.RootCommand> roots = dispatcher.scan(commandContainer);

        for (CommandDispatcher.RootCommand root : roots) {
            Command bungeeCmd = new BungeeWrappedCommand(root.name, root.aliases, dispatcher);
            plugin.getProxy().getPluginManager().registerCommand(plugin, bungeeCmd);
        }

        return this;
    }

    public BungeeCommandManager register(Object... commands) {
        for (Object command : commands) {
            register(command);
        }
        return this;
    }

    public <T> BungeeCommandManager registerProvider(Class<T> type, Provider<T> provider) {
        providerRegistry.register(type, provider);
        return this;
    }

    private static final class BungeeWrappedCommand extends Command implements TabExecutor {
        private final String commandName;
        private final CommandDispatcher dispatcher;

        BungeeWrappedCommand(String name, String[] aliases, CommandDispatcher dispatcher) {
            super(name, null, aliases);
            this.commandName = name;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            BungeeCommandActor actor = new BungeeCommandActor(sender);
            dispatcher.handleCommand(commandName, actor, commandName, args);
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            BungeeCommandActor actor = new BungeeCommandActor(sender);
            return dispatcher.handleTabComplete(commandName, actor, args);
        }
    }
}
