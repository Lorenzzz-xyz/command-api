package dev.lorenzz.bungee;

import dev.lorenzz.common.CommandActor;
import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.exception.CommandExitException;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class BungeeProviderDefaults {

    private BungeeProviderDefaults() {
    }

    public static void register(ProviderRegistry registry, LanguageProvider lang, ColorTranslator colors) {
        registry.register(ProxiedPlayer.class, new Provider<ProxiedPlayer>() {
            @Override
            public ProxiedPlayer provide(String input) throws CommandExitException {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(input);
                if (player == null) {
                    throw new CommandExitException(colors.translate(lang.offlinePlayer(input)));
                }
                return player;
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                String lower = arg.toLowerCase();
                return ProxyServer.getInstance().getPlayers().stream()
                        .map(ProxiedPlayer::getName)
                        .filter(name -> name.toLowerCase().startsWith(lower))
                        .collect(Collectors.toList());
            }
        });
    }
}
