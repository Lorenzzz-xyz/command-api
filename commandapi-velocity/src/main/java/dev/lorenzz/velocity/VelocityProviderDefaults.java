package dev.lorenzz.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.lorenzz.common.CommandActor;
import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.exception.CommandExitException;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;

import java.util.List;
import java.util.stream.Collectors;

public final class VelocityProviderDefaults {

    private VelocityProviderDefaults() {
    }

    public static void register(ProviderRegistry registry, ProxyServer server, LanguageProvider lang, ColorTranslator colors) {
        registry.register(Player.class, new Provider<Player>() {
            @Override
            public Player provide(String input) throws CommandExitException {
                Player player = server.getPlayer(input).orElse(null);
                if (player == null) {
                    throw new CommandExitException(colors.translate(lang.offlinePlayer(input)));
                }
                return player;
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                String lower = arg.toLowerCase();
                return server.getAllPlayers().stream()
                        .map(Player::getUsername)
                        .filter(name -> name.toLowerCase().startsWith(lower))
                        .collect(Collectors.toList());
            }
        });
    }
}
