package dev.lorenzz.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.lorenzz.common.dispatch.CommandDispatcher;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;

import java.util.List;

public final class VelocityCommandManager {

    private final ProxyServer server;
    private final Object plugin;
    private final VelocityCommandPlatform platform;
    private final ProviderRegistry providerRegistry;
    private final CommandDispatcher dispatcher;

    public VelocityCommandManager(ProxyServer server, Object plugin) {
        VelocityLanguageProvider languageProvider = new VelocityLanguageProvider();
        this.server = server;
        this.plugin = plugin;
        this.platform = new VelocityCommandPlatform(server, plugin, languageProvider);
        this.providerRegistry = new ProviderRegistry();
        this.providerRegistry.registerDefaults(languageProvider, platform.getColorTranslator());
        VelocityProviderDefaults.register(providerRegistry, server, languageProvider, platform.getColorTranslator());
        this.dispatcher = new CommandDispatcher(platform, providerRegistry);
    }

    public VelocityCommandManager register(Object commandContainer) {
        List<CommandDispatcher.RootCommand> roots = dispatcher.scan(commandContainer);

        for (CommandDispatcher.RootCommand root : roots) {
            SimpleCommand velocityCmd = new VelocityWrappedCommand(root.name, dispatcher);
            com.velocitypowered.api.command.CommandMeta meta = server.getCommandManager()
                    .metaBuilder(root.name)
                    .aliases(root.aliases)
                    .plugin(plugin)
                    .build();
            server.getCommandManager().register(meta, velocityCmd);
        }

        return this;
    }

    public VelocityCommandManager register(Object... commands) {
        for (Object command : commands) {
            register(command);
        }
        return this;
    }

    public <T> VelocityCommandManager registerProvider(Class<T> type, Provider<T> provider) {
        providerRegistry.register(type, provider);
        return this;
    }

    private static final class VelocityWrappedCommand implements SimpleCommand {
        private final String commandName;
        private final CommandDispatcher dispatcher;

        VelocityWrappedCommand(String name, CommandDispatcher dispatcher) {
            this.commandName = name;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(Invocation invocation) {
            VelocityCommandActor actor = new VelocityCommandActor(invocation.source());
            dispatcher.handleCommand(commandName, actor, invocation.alias(), invocation.arguments());
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            VelocityCommandActor actor = new VelocityCommandActor(invocation.source());
            return dispatcher.handleTabComplete(commandName, actor, invocation.arguments());
        }
    }
}
