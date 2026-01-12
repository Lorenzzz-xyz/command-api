package dev.lorenzz.commandapi.registration;

import dev.lorenzz.commandapi.annotations.*;
import dev.lorenzz.commandapi.annotations.Optional;
import dev.lorenzz.commandapi.annotations.*;
import dev.lorenzz.commandapi.exception.CommandException;
import dev.lorenzz.commandapi.exception.CommandExitException;
import dev.lorenzz.commandapi.help.HelpGenerator;
import dev.lorenzz.commandapi.help.PaginatedHelp;
import dev.lorenzz.commandapi.provider.Provider;
import dev.lorenzz.commandapi.provider.ProviderRegistry;
import dev.lorenzz.commandapi.util.CommandUtils;
import dev.lorenzz.commandapi.util.LanguageLocale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class CommandRegistrar {

    private final PluginCommandFactory factory = new PluginCommandFactory();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final org.bukkit.plugin.Plugin plugin;
    private final CommandMap commandMap;

    public CommandRegistrar(org.bukkit.plugin.Plugin plugin, CommandMap commandMap) {
        this.plugin = plugin;
        this.commandMap = commandMap;
    }

    public void register(Object container) {
        Map<String, RootCommand> rootsByName = new HashMap<>();
        Map<Class<?>, RootCommand> rootsByClass = new HashMap<>();

        for (Method method : container.getClass().getDeclaredMethods()) {
            Command root = method.getAnnotation(Command.class);
            Subcommand sub = method.getAnnotation(Subcommand.class);

            if (root != null && sub != null) {
                throw new CommandException(LanguageLocale.COMMAND_NOT_REGISTERED_CORRECTLY.getFormattedString(root.name()));
            }

            if (root != null) {
                RootCommand rc = buildRoot(container, method, root);

                rootsByName.put(root.name().toLowerCase(Locale.ROOT), rc);
                rootsByClass.put(container.getClass(), rc);
            }
        }

        for (Method method : container.getClass().getDeclaredMethods()) {
            Subcommand sub = method.getAnnotation(Subcommand.class);
            if (sub == null) continue;

            RootCommand rootCmd = rootsByClass.get(container.getClass());
            if (rootCmd == null) {
                throw new CommandException(LanguageLocale.COMMAND_NOT_REGISTERED_CORRECTLY.getFormattedString(sub.name()));
            }

            rootCmd.addSubcommand(buildSub(container, method, sub));
        }

        for (RootCommand root : rootsByName.values()) {
            registerRoot(root);
        }
    }

    private RootCommand buildRoot(Object container, Method method, Command ann) {
        Permission perm = method != null ? method.getAnnotation(Permission.class) : null;
        MethodHandle handle = method != null ? unreflect(method, container) : null;

        CommandMeta meta = method != null ? buildMeta(method) : null;
        String usage = method != null ? HelpGenerator.usage(ann.name(), "", ann.usage(), method.getParameters()) : ann.usage();

        return new RootCommand(container, handle, meta, ann, perm, usage);
    }

    private SubMeta buildSub(Object container, Method method, Subcommand ann) {
        Permission perm = method.getAnnotation(Permission.class);
        MethodHandle handle = unreflect(method, container);

        CommandMeta meta = buildMeta(method);
        String usage = HelpGenerator.usage("", ann.name(), "", method.getParameters());

        return new SubMeta(container, handle, meta, ann, perm, usage);
    }

    private CommandMeta buildMeta(Method method) {
        Parameter[] parameters = method.getParameters();
        List<ParamMeta> meta = new ArrayList<>();

        for (Parameter parameter : parameters) {
            ParamMeta pm = new ParamMeta(parameter);
            meta.add(pm);
        }

        return new CommandMeta(meta, method);
    }

    private MethodHandle unreflect(Method method, Object target) {
        try {
            method.setAccessible(true);
            MethodHandle handle = lookup.unreflect(method);

            return handle.bindTo(target);
        } catch (IllegalAccessException e) {
            throw new CommandException("Cannot access method " + method.getName(), e);
        }
    }

    private void registerRoot(RootCommand root) {
        org.bukkit.command.Command existing = commandMap.getCommand(root.name);
        if (existing != null) {
            existing.unregister(commandMap);
        }

        org.bukkit.command.Command bukkitCommand = factory.create(root.name, plugin, root.aliases, (sender, label, args) -> {
            if (!root.subcommands.isEmpty()) {
                if (args.length == 0) {
                    sendHelp(sender, root, 1);
                    return true;
                }

                String subName = args[0].toLowerCase(Locale.ROOT);
                SubMeta sub = root.subcommands.get(subName);

                if (sub == null) {
                    sendHelp(sender, root, 1);
                    return true;
                }

                String[] tail = Arrays.copyOfRange(args, 1, args.length);
                execute(root, sub, sender, label, tail);

                return true;
            } else {
                execute(root, null, sender, label, args);
                return true;
            }
        }, (sender, alias, args) -> {
            if (!root.subcommands.isEmpty()) {
                if (args.length == 1) {
                    List<String> names = new ArrayList<>(root.subcommands.keySet());
                    names.sort(String::compareToIgnoreCase);

                    return names;
                }

                String subName = args[0].toLowerCase(Locale.ROOT);
                SubMeta sub = root.subcommands.get(subName);

                if (sub == null) return Collections.emptyList();
                return tabComplete(sub.meta, sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                return tabComplete(root.meta, sender, args);
            }
        });

        bukkitCommand.setDescription(root.description);
        bukkitCommand.setUsage(root.usage);

        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), bukkitCommand);
    }

    private void sendHelp(CommandSender sender, RootCommand root, int page) {
        List<HelpGenerator.HelpEntry> entries = new ArrayList<>();
        for (SubMeta sub : root.subcommands.values()) {
            entries.add(new HelpGenerator.HelpEntry(sub.ann.name(), sub.usage, sub.ann.description()));
        }

        PaginatedHelp help = HelpGenerator.subcommandHelp(root.name, entries);
        int pages = help.pages();
        List<String> lines = help.page(page);

        sender.sendMessage(CommandUtils.colorize(LanguageLocale.HELP_COMMAND_HEADER.getString().replace("<commandapi>", root.name).replace("<page>", String.valueOf(page)).replace("<max>", String.valueOf(pages))));
        for (String line : lines) {
            sender.sendMessage(line);
        }
        sender.sendMessage(CommandUtils.colorize(LanguageLocale.HELP_COMMAND_FOOTER.getString().replace("<count>", String.valueOf(entries.size())).replace("<commandapi>", root.name)));
    }

    private void execute(RootCommand root, SubMeta sub, CommandSender sender, String label, String[] args) {
        CommandMeta meta = sub != null ? sub.meta : root.meta;
        Permission perm = sub != null ? sub.permission : root.permission;
        boolean async = sub != null ? sub.ann.async() : root.ann.async();
        MethodHandle handle = sub != null ? sub.handle : root.handle;
        String usage = sub != null ? sub.usage : root.usage;

        if (perm != null && perm.value() != null && !perm.value().isEmpty() && !sender.hasPermission(perm.value())) {
            sender.sendMessage(CommandUtils.colorize(LanguageLocale.NO_PERMISSION.getString()));
            return;
        }

        Runnable task = () -> {
            try {
                Object[] params = buildParameters(meta, sender, args, usage);
                handle.invokeWithArguments(params);
            } catch (CommandExitException ex) {
                sender.sendMessage(CommandUtils.colorize(ex.getMessage()));
            } catch (Throwable t) {
                throw new CommandException(CommandUtils.colorize(LanguageLocale.ERROR_OCCURRED.getString()), t);
            }
        };

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        } else {
            task.run();
        }
    }

    private Object[] buildParameters(CommandMeta meta, CommandSender sender, String[] args, String usage) throws CommandExitException {
        List<Object> list = new ArrayList<>(meta.params.size());
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        Map<String, Boolean> flags = new HashMap<>();

        for (ParamMeta pm : meta.params) {
            if (pm.flag != null) {
                boolean present = argList.removeIf(s -> s.equalsIgnoreCase(pm.flag));

                flags.put(pm.flag, present);
            }
        }

        int argIndex = 0;
        for (ParamMeta pm : meta.params) {
            Class<?> type = pm.type;

            if (pm.sender) {
                if (type.isAssignableFrom(CommandSender.class)) {
                    list.add(sender);
                } else if (type.isAssignableFrom(Player.class)) {
                    if (!(sender instanceof Player)) throw new CommandExitException(CommandUtils.colorize(LanguageLocale.PLAYERS_ONLY.getString()));
                    list.add(sender);
                } else if (type.isAssignableFrom(ConsoleCommandSender.class)) {
                    if (!(sender instanceof ConsoleCommandSender)) throw new CommandExitException(CommandUtils.colorize(LanguageLocale.CONSOLES_ONLY.getString()));
                    list.add(sender);
                } else {
                    throw new CommandExitException("Invalid sender type.");
                }

                continue;
            }

            if (pm.flag != null) {
                list.add(flags.getOrDefault(pm.flag, false));
                continue;
            }

            String raw;
            if (pm.combined) {
                if (argIndex >= argList.size()) {
                    if (pm.optional) {
                        raw = pm.optionalDefault != null ? pm.optionalDefault : "";
                    } else {
                        throw new CommandExitException(usage.toLowerCase().contains("usage") ? CommandUtils.colorize(usage) : ChatColor.RED + "Usage: " + usage);
                    }
                } else {
                    raw = String.join(" ", argList.subList(argIndex, argList.size()));
                    argIndex = argList.size();
                }
            } else {
                raw = argIndex < argList.size() ? argList.get(argIndex) : null;

                if (raw == null || raw.isEmpty()) {
                    if (pm.optional) {
                        if (pm.optionalDefault != null && !pm.optionalDefault.isEmpty()) {
                            raw = pm.optionalDefault;
                        } else {
                            list.add(defaultValue(type));
                            continue;
                        }
                    } else {
                        throw new CommandExitException((usage.toLowerCase().contains("usage") ? "" : ChatColor.RED + "Usage: ") + usage);
                    }
                } else {
                    argIndex++;
                }
            }

            Provider<?> provider = ProviderRegistry.get(type);
            if (provider == null) throw new CommandExitException("No me.empire.commandapi.provider for " + type.getSimpleName());

            Object provided = provider.provide(raw);
            HelpGenerator.validateRanges(pm.parameter, provided);

            list.add(provided);
        }

        return list.toArray(new Object[0]);
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        if (type == float.class) return 0F;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return (char) 0;

        return null;
    }

    private List<String> tabComplete(CommandMeta meta, CommandSender sender, String[] args) {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        List<String> completions = new ArrayList<>();

        int argIndex = 0;
        for (ParamMeta pm : meta.params) {
            if (pm.flag != null) {
                completions.add(pm.flag);
                continue;
            }

            if (pm.combined) {
                Provider<?> provider = ProviderRegistry.get(pm.type);
                if (provider == null) return Collections.emptyList();

                return provider.tabComplete(sender, String.join(" ", argList));
            }

            if (argIndex == argList.size()) {
                Provider<?> provider = ProviderRegistry.get(pm.type);
                if (provider == null) return Collections.emptyList();

                return provider.tabComplete(sender, "");
            } else {
                argIndex++;
            }
        }

        return completions;
    }

    private static final class CommandMeta {
        private final List<ParamMeta> params;
        private final Method method;
        private final String usage;

        private CommandMeta(List<ParamMeta> params, Method method) {
            this.params = params;
            this.method = method;
            this.usage = null;
        }
    }

    private static final class ParamMeta {
        private final Parameter parameter;
        private final Class<?> type;
        private final boolean sender;
        private final boolean optional;
        private final String optionalDefault;
        private final boolean hidden;
        private final String name;
        private final String flag;
        private final boolean combined;

        private ParamMeta(Parameter parameter) {
            this.parameter = parameter;
            this.type = parameter.getType();
            this.sender = parameter.isAnnotationPresent(Sender.class);
            Optional opt = parameter.getAnnotation(Optional.class);
            this.optional = opt != null;
            this.optionalDefault = opt != null ? opt.value() : null;
            this.hidden = parameter.isAnnotationPresent(Hidden.class);
            Named named = parameter.getAnnotation(Named.class);
            this.name = named != null ? named.value() : parameter.getName();
            Flag f = parameter.getAnnotation(Flag.class);
            this.flag = f != null ? f.value() : null;
            this.combined = parameter.isAnnotationPresent(Combined.class);
        }
    }

    private static final class RootCommand {
        private final Object instance;
        private final MethodHandle handle;
        private final CommandMeta meta;
        private final Command ann;
        private final Permission permission;
        private final String name;
        private final String[] aliases;
        private final String usage;
        private final String description;
        private final Map<String, SubMeta> subcommands = new HashMap<>();

        private RootCommand(Object instance, MethodHandle handle, CommandMeta meta, Command ann, Permission permission, String usage) {
            this.instance = instance;
            this.handle = handle;
            this.meta = meta;
            this.ann = ann;
            this.permission = permission;
            this.name = ann.name();
            this.aliases = ann.aliases();
            this.usage = usage;
            this.description = ann.description();
        }

        private void addSubcommand(SubMeta sub) {
            subcommands.put(sub.ann.name().toLowerCase(Locale.ROOT), sub);
        }
    }

    private static final class SubMeta {
        private final Object instance;
        private final MethodHandle handle;
        private final CommandMeta meta;
        private final Subcommand ann;
        private final Permission permission;
        private final String usage;

        private SubMeta(Object instance, MethodHandle handle, CommandMeta meta, Subcommand ann, Permission permission, String usage) {
            this.instance = instance;
            this.handle = handle;
            this.meta = meta;
            this.ann = ann;
            this.permission = permission;
            this.usage = usage;
        }
    }
}