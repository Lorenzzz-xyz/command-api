package dev.lorenzz.bungee;

import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.CommandPlatform;
import dev.lorenzz.common.LanguageProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.stream.Collectors;

public final class BungeeCommandPlatform implements CommandPlatform {

    private final Plugin plugin;
    private final ColorTranslator colorTranslator;
    private final LanguageProvider languageProvider;

    public BungeeCommandPlatform(Plugin plugin, LanguageProvider languageProvider) {
        this.plugin = plugin;
        this.colorTranslator = new BungeeColorTranslator();
        this.languageProvider = languageProvider;
    }

    @Override
    public void runAsync(Runnable task) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
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
}
