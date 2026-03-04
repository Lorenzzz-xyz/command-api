package dev.lorenzz.bukkit;

import dev.lorenzz.common.CommandActor;
import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.exception.CommandExitException;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitProviderDefaults {

    private BukkitProviderDefaults() {
    }

    public static void register(ProviderRegistry registry, LanguageProvider lang, ColorTranslator colors) {
        registry.register(Player.class, new Provider<Player>() {
            @Override
            public Player provide(String input) throws CommandExitException {
                Player player = Bukkit.getPlayer(input);
                if (player == null) {
                    throw new CommandExitException(colors.translate(lang.playerNeverJoined(input)));
                }
                if (!player.isOnline()) {
                    throw new CommandExitException(colors.translate(lang.offlinePlayer(input)));
                }
                return player;
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                String lower = arg.toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(lower))
                        .collect(Collectors.toList());
            }
        });

        registry.register(OfflinePlayer.class, new Provider<OfflinePlayer>() {
            @Override
            public OfflinePlayer provide(String input) throws CommandExitException {
                @SuppressWarnings("deprecation")
                OfflinePlayer player = Bukkit.getOfflinePlayer(input);
                if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
                    throw new CommandExitException(colors.translate(lang.playerNeverJoined(input)));
                }
                return player;
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                String lower = arg.toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(lower))
                        .collect(Collectors.toList());
            }
        });
    }
}
