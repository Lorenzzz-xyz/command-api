package dev.lorenzz.bukkit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import dev.lorenzz.common.annotations.*;
import dev.lorenzz.common.dispatch.CommandDispatcher.CommandMeta;
import dev.lorenzz.common.dispatch.CommandDispatcher.ParamMeta;
import dev.lorenzz.common.dispatch.CommandDispatcher.RootCommand;
import dev.lorenzz.common.dispatch.CommandDispatcher.SubMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

final class BrigadierSupport {

    private static boolean available;

    static {
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
            available = true;
        } catch (ClassNotFoundException ignored) {
            available = false;
        }
    }

    private BrigadierSupport() {
    }

    static boolean isAvailable() {
        return available;
    }

    @SuppressWarnings("unchecked")
    static void register(RootCommand root) {
        if (!available) return;

        try {
            Object nmsServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());

            CommandDispatcher<Object> brigadierDispatcher = null;
            for (Method m : nmsServer.getClass().getMethods()) {
                if (m.getReturnType() == com.mojang.brigadier.CommandDispatcher.class && m.getParameterCount() == 0) {
                    brigadierDispatcher = (CommandDispatcher<Object>) m.invoke(nmsServer);
                    break;
                }
            }

            if (brigadierDispatcher == null) {
                for (Field f : nmsServer.getClass().getDeclaredFields()) {
                    if (f.getType().getName().contains("Commands") || f.getType().getName().contains("CommandDispatcher")) {
                        f.setAccessible(true);
                        Object commands = f.get(nmsServer);
                        for (Method m : commands.getClass().getMethods()) {
                            if (m.getReturnType() == com.mojang.brigadier.CommandDispatcher.class && m.getParameterCount() == 0) {
                                brigadierDispatcher = (CommandDispatcher<Object>) m.invoke(commands);
                                break;
                            }
                        }
                        if (brigadierDispatcher != null) break;
                    }
                }
            }

            if (brigadierDispatcher == null) return;

            LiteralArgumentBuilder<Object> literal = LiteralArgumentBuilder.literal(root.name);

            if (!root.subcommands.isEmpty()) {
                for (Map.Entry<String, SubMeta> entry : root.subcommands.entrySet()) {
                    LiteralArgumentBuilder<Object> subLiteral = LiteralArgumentBuilder.literal(entry.getKey());
                    appendParameters(subLiteral, entry.getValue().meta);
                    literal.then(subLiteral);
                }
            } else {
                appendParameters(literal, root.meta);
            }

            brigadierDispatcher.getRoot().addChild(literal.build());

            for (String alias : root.aliases) {
                LiteralArgumentBuilder<Object> aliasLiteral = LiteralArgumentBuilder.literal(alias);
                aliasLiteral.redirect(brigadierDispatcher.getRoot().getChild(root.name));
                brigadierDispatcher.getRoot().addChild(aliasLiteral.build());
            }

        } catch (Exception ignored) {
        }
    }

    static void syncCommands() {
        if (!available) return;
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        } catch (Exception ignored) {
        }
    }

    private static void appendParameters(LiteralArgumentBuilder<Object> builder, CommandMeta meta) {
        CommandNode<Object> current = null;

        for (ParamMeta pm : meta.params) {
            if (pm.sender) continue;
            if (pm.hidden) continue;

            if (pm.flag != null) {
                LiteralArgumentBuilder<Object> flagLiteral = LiteralArgumentBuilder.literal(pm.flag);
                if (current == null) {
                    builder.then(flagLiteral);
                }
                continue;
            }

            ArgumentType<?> argType = resolveArgumentType(pm);
            RequiredArgumentBuilder<Object, ?> arg = RequiredArgumentBuilder.argument(pm.name, argType);

            if (pm.type == Player.class || pm.type.getSimpleName().equals("Player")) {
                arg.suggests(playerSuggestions());
            }

            if (pm.type.isEnum()) {
                arg.suggests(enumSuggestions(pm.type));
            }

            if (pm.type == boolean.class || pm.type == Boolean.class) {
                arg.suggests(booleanSuggestions());
            }

            if (current == null) {
                builder.then(arg);
                current = arg.build();
            } else {
                builder.then(arg);
            }
        }
    }

    private static ArgumentType<?> resolveArgumentType(ParamMeta pm) {
        Class<?> type = pm.type;
        Range range = pm.parameter.getAnnotation(Range.class);

        if (type == int.class || type == Integer.class) {
            if (range != null) {
                return IntegerArgumentType.integer((int) range.min(), (int) range.max());
            }
            return IntegerArgumentType.integer();
        }

        if (type == long.class || type == Long.class) {
            if (range != null) {
                return LongArgumentType.longArg(range.min(), range.max());
            }
            return LongArgumentType.longArg();
        }

        if (type == double.class || type == Double.class) {
            if (range != null) {
                return DoubleArgumentType.doubleArg(range.min(), range.max());
            }
            return DoubleArgumentType.doubleArg();
        }

        if (type == float.class || type == Float.class) {
            if (range != null) {
                return FloatArgumentType.floatArg((float) range.min(), (float) range.max());
            }
            return FloatArgumentType.floatArg();
        }

        if (type == boolean.class || type == Boolean.class) {
            return BoolArgumentType.bool();
        }

        if (pm.combined) {
            return StringArgumentType.greedyString();
        }

        return StringArgumentType.word();
    }

    private static SuggestionProvider<Object> playerSuggestions() {
        return (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(remaining)) {
                    builder.suggest(player.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<Object> enumSuggestions(Class<?> enumType) {
        return (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase();
            for (Object constant : enumType.getEnumConstants()) {
                String name = ((Enum<?>) constant).name().toLowerCase();
                if (name.startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<Object> booleanSuggestions() {
        return (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase();
            if ("true".startsWith(remaining)) builder.suggest("true");
            if ("false".startsWith(remaining)) builder.suggest("false");
            return builder.buildFuture();
        };
    }
}
