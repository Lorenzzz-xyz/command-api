package dev.lorenzz.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.CommandPlatform;
import dev.lorenzz.common.LanguageProvider;

import java.util.Collection;
import java.util.stream.Collectors;

public final class VelocityCommandPlatform implements CommandPlatform {

    private final ProxyServer server;
    private final Object plugin;
    private final ColorTranslator colorTranslator;
    private final LanguageProvider languageProvider;

    public VelocityCommandPlatform(ProxyServer server, Object plugin, LanguageProvider languageProvider) {
        this.server = server;
        this.plugin = plugin;
        this.colorTranslator = new VelocityColorTranslator();
        this.languageProvider = languageProvider;
    }

    @Override
    public void runAsync(Runnable task) {
        server.getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return server.getAllPlayers().stream()
                .map(Player::getUsername)
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

    public ProxyServer getServer() {
        return server;
    }
}
