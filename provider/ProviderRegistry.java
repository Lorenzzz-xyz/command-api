package dev.lorenzz.commandapi.provider;

import dev.lorenzz.commandapi.exception.CommandExitException;
import dev.lorenzz.commandapi.util.CommandUtils;
import dev.lorenzz.commandapi.util.LanguageLocale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ProviderRegistry {
    private static final Map<Class<?>, Provider<?>> REGISTRY = new ConcurrentHashMap<>();

    private ProviderRegistry() {
    }

    public static <T> void register(Class<T> type, Provider<T> provider) {
        REGISTRY.put(type, provider);
    }

    @SuppressWarnings("unchecked")
    public static <T> Provider<T> get(Class<T> type) {
        Provider<?> provider = REGISTRY.get(type);

        if (provider == null && type.isEnum()) {
            provider = enumProvider(type);
            REGISTRY.put(type, provider);
        }

        return (Provider<T>) provider;
    }

    public static void registerDefaults() {
        register(String.class, new Provider<String>() {
            @Override
            public String provide(String input) {
                return input;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                return Collections.emptyList();
            }
        });

        register(Integer.class, numeric(Integer::valueOf));
        register(int.class, get(Integer.class));

        register(Long.class, numeric(Long::valueOf));
        register(long.class, get(Long.class));

        register(Double.class, numeric(Double::valueOf));
        register(double.class, get(Double.class));

        register(Float.class, numeric(Float::valueOf));
        register(float.class, get(Float.class));

        register(Boolean.class, new Provider<Boolean>() {
            @Override
            public Boolean provide(String input) throws CommandExitException {
                if (input == null) throw new CommandExitException("Missing boolean");

                if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes")) return true;
                if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no")) return false;

                throw new CommandExitException(CommandUtils.colorize(LanguageLocale.INVALID_BOOLEAN.getString()));
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                return Arrays.asList("true", "false");
            }
        });
        register(boolean.class, get(Boolean.class));

        register(OfflinePlayer.class, new Provider<OfflinePlayer>() {
            @Override
            public OfflinePlayer provide(String input) throws CommandExitException {
                OfflinePlayer player = Bukkit.getOfflinePlayer(input);

                if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
                    throw new CommandExitException(CommandUtils.colorize(LanguageLocale.PLAYER_NEVER_JOINED.getFormattedString(input)));
                }

                return player;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        });
        register(Player.class, new Provider<Player>() {
            @Override
            public Player provide(String input) throws CommandExitException {
                Player player = Bukkit.getPlayer(input);

                if (player == null) {
                    throw new CommandExitException(CommandUtils.colorize(LanguageLocale.PLAYER_NEVER_JOINED.getFormattedString(input)));
                }

                if (!player.isOnline()) {
                    throw new CommandExitException(CommandUtils.colorize(LanguageLocale.OFFLINE_PLAYER.getFormattedString(input)));
                }

                return player;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        });
    }

    private static <N> Provider<N> numeric(NumericParser<N> parser) {
        return new Provider<N>() {
            @Override
            public N provide(String input) throws CommandExitException {
                try {
                    return parser.parse(input);
                } catch (Exception ex) {
                    throw new CommandExitException(CommandUtils.colorize(LanguageLocale.INVALID_NUMBER.getFormattedString(input)));
                }
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                return Collections.emptyList();
            }
        };
    }

    private static Provider<?> enumProvider(Class<?> type) {
        Object[] constants = type.getEnumConstants();

        return new Provider<Enum<?>>() {
            @Override
            public Enum<?> provide(String input) throws CommandExitException {
                for (Object constant : constants) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(input)) {
                        return (Enum<?>) constant;
                    }
                }

                throw new CommandExitException(CommandUtils.colorize(LanguageLocale.INVALID_ENUM.getFormattedString(input, String.join(", ", Arrays.stream(constants)
                        .map(constant -> ((Enum<?>) constant).name().toLowerCase())
                        .collect(Collectors.toList())))));
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String arg) {
                List<String> list = new ArrayList<>();

                for (Object constant : constants) {
                    list.add(((Enum<?>) constant).name().toLowerCase(Locale.ROOT));
                }

                return list;
            }
        };
    }

    private interface NumericParser<N> {
        N parse(String input) throws Exception;
    }
}