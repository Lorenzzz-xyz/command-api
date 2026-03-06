package dev.lorenzz.common.dispatch;

import dev.lorenzz.common.*;
import dev.lorenzz.common.annotations.*;
import dev.lorenzz.common.annotations.Optional;
import dev.lorenzz.common.exception.CommandException;
import dev.lorenzz.common.exception.CommandExitException;
import dev.lorenzz.common.help.HelpGenerator;
import dev.lorenzz.common.help.PaginatedHelp;
import dev.lorenzz.common.provider.Provider;
import dev.lorenzz.common.provider.ProviderRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandDispatcher {

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final CommandPlatform platform;
    private final ProviderRegistry providerRegistry;
    private final ColorTranslator colors;
    private final LanguageProvider lang;
    private final Map<String, RootCommand> roots = new LinkedHashMap<>();

    public CommandDispatcher(CommandPlatform platform, ProviderRegistry providerRegistry) {
        this.platform = platform;
        this.providerRegistry = providerRegistry;
        this.colors = platform.getColorTranslator();
        this.lang = platform.getLanguageProvider();
    }

    public List<RootCommand> scan(Object container) {
        Map<String, RootCommand> scannedRoots = new LinkedHashMap<>();
        List<Method> subMethods = new ArrayList<>();

        for (Method method : container.getClass().getDeclaredMethods()) {
            Command root = method.getAnnotation(Command.class);
            Subcommand sub = method.getAnnotation(Subcommand.class);

            if (root != null && sub != null) {
                throw new CommandException(colors.translate(lang.commandNotRegistered(root.name())));
            }

            if (root != null) {
                RootCommand rc = buildRoot(container, method, root);
                scannedRoots.put(root.name().toLowerCase(Locale.ROOT), rc);
            } else if (sub != null) {
                subMethods.add(method);
            }
        }

        RootCommand containerRoot = null;
        for (RootCommand rc : scannedRoots.values()) {
            containerRoot = rc;
            break; // first root in the container
        }

        for (Method method : subMethods) {
            Subcommand sub = method.getAnnotation(Subcommand.class);
            if (containerRoot == null) {
                throw new CommandException(colors.translate(lang.commandNotRegistered(sub.name())));
            }
            containerRoot.addSubcommand(buildSub(container, method, sub));
        }

        roots.putAll(scannedRoots);
        return new ArrayList<>(scannedRoots.values());
    }

    public void handleCommand(String rootName, CommandActor actor, String label, String[] args) {
        RootCommand root = roots.get(rootName.toLowerCase(Locale.ROOT));
        if (root == null) return;

        if (!root.subcommands.isEmpty()) {
            if (args.length == 0) {
                sendHelp(actor, root, 1);
                return;
            }

            String subName = args[0].toLowerCase(Locale.ROOT);
            SubMeta sub = root.subcommands.get(subName);

            if (sub == null) {
                sendHelp(actor, root, 1);
                return;
            }

            String[] tail = Arrays.copyOfRange(args, 1, args.length);
            execute(root, sub, actor, label, tail);
        } else {
            execute(root, null, actor, label, args);
        }
    }

    public List<String> handleTabComplete(String rootName, CommandActor actor, String[] args) {
        RootCommand root = roots.get(rootName.toLowerCase(Locale.ROOT));
        if (root == null) return Collections.emptyList();

        if (!root.subcommands.isEmpty()) {
            if (args.length <= 1) {
                String partial = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
                return root.subcommands.keySet().stream()
                        .filter(name -> name.startsWith(partial))
                        .sorted(String::compareToIgnoreCase)
                        .collect(Collectors.toList());
            }

            String subName = args[0].toLowerCase(Locale.ROOT);
            SubMeta sub = root.subcommands.get(subName);
            if (sub == null) return Collections.emptyList();

            return tabComplete(sub.meta, actor, Arrays.copyOfRange(args, 1, args.length));
        } else {
            return tabComplete(root.meta, actor, args);
        }
    }

    private RootCommand buildRoot(Object container, Method method, Command ann) {
        Permission perm = method.getAnnotation(Permission.class);
        MethodHandle handle = unreflect(method, container);
        CommandMeta meta = buildMeta(method);
        String usage = HelpGenerator.usage(ann.name(), "", ann.usage(), method.getParameters());

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
            meta.add(new ParamMeta(parameter));
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

    private void sendHelp(CommandActor actor, RootCommand root, int page) {
        List<HelpGenerator.HelpEntry> entries = new ArrayList<>();
        for (SubMeta sub : root.subcommands.values()) {
            entries.add(new HelpGenerator.HelpEntry(sub.ann.name(), sub.usage, sub.ann.description()));
        }

        PaginatedHelp help = HelpGenerator.subcommandHelp(root.name, entries, lang, colors);
        int pages = help.pages();
        List<String> lines = help.page(page);

        actor.sendMessage(colors.translate(lang.helpHeader(root.name, page, pages)));
        for (String line : lines) {
            actor.sendMessage(line);
        }
        actor.sendMessage(colors.translate(lang.helpFooter(root.name, entries.size())));
    }

    private void execute(RootCommand root, SubMeta sub, CommandActor actor, String label, String[] args) {
        CommandMeta meta = sub != null ? sub.meta : root.meta;
        Permission perm = sub != null ? sub.permission : root.permission;
        boolean async = sub != null ? sub.ann.async() : root.ann.async();
        MethodHandle handle = sub != null ? sub.handle : root.handle;
        String usage = sub != null ? sub.usage : root.usage;

        if (perm != null && perm.value() != null && !perm.value().isEmpty() && !actor.hasPermission(perm.value())) {
            actor.sendMessage(colors.translate(lang.noPermission()));
            return;
        }

        Runnable task = () -> {
            try {
                Object[] params = buildParameters(meta, actor, args, usage);
                handle.invokeWithArguments(params);
            } catch (CommandExitException ex) {
                actor.sendMessage(colors.translate(ex.getMessage()));
            } catch (Throwable t) {
                throw new CommandException(colors.translate(lang.errorOccurred()), t);
            }
        };

        if (async) {
            platform.runAsync(task);
        } else {
            task.run();
        }
    }

    private Object[] buildParameters(CommandMeta meta, CommandActor actor, String[] args, String usage) throws CommandExitException {
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
                if (CommandActor.class.isAssignableFrom(type)) {
                    list.add(actor);
                } else {
                    try {
                        Object unwrapped = actor.unwrap(type);
                        if (unwrapped == null) {
                            if (actor.isConsole()) {
                                throw new CommandExitException(colors.translate(lang.playersOnly()));
                            } else {
                                throw new CommandExitException(colors.translate(lang.consolesOnly()));
                            }
                        }
                        list.add(unwrapped);
                    } catch (ClassCastException e) {
                        throw new CommandExitException("Invalid sender type.");
                    }
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
                        throw new CommandExitException("&cUsage: " + usage);
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
                        throw new CommandExitException("&cUsage: " + usage);
                    }
                } else {
                    argIndex++;
                }
            }

            Provider<?> provider = providerRegistry.get(type);
            if (provider == null) {
                throw new CommandExitException("No provider for " + type.getSimpleName());
            }

            Object provided = provider.provide(raw);
            HelpGenerator.validateRanges(pm.parameter, provided, lang, colors);
            list.add(provided);
        }

        return list.toArray(new Object[0]);
    }

    private List<String> tabComplete(CommandMeta meta, CommandActor actor, String[] args) {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        List<String> completions = new ArrayList<>();
        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";

        int argIndex = 0;
        for (ParamMeta pm : meta.params) {
            if (pm.sender) continue;

            if (pm.flag != null) {
                if (pm.flag.toLowerCase(Locale.ROOT).startsWith(lastArg)) {
                    completions.add(pm.flag);
                }
                continue;
            }

            if (pm.combined) {
                Provider<?> provider = providerRegistry.get(pm.type);
                if (provider == null) return Collections.emptyList();

                return filterCompletions(provider.tabComplete(actor, String.join(" ", argList)), lastArg);
            }

            if (argIndex == argList.size() - 1) {
                Provider<?> provider = providerRegistry.get(pm.type);
                if (provider == null) return Collections.emptyList();

                return filterCompletions(provider.tabComplete(actor, lastArg), lastArg);
            } else if (argIndex < argList.size()) {
                argIndex++;
            } else {
                Provider<?> provider = providerRegistry.get(pm.type);
                if (provider == null) return Collections.emptyList();

                return filterCompletions(provider.tabComplete(actor, ""), "");
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String partial) {
        if (partial.isEmpty()) return completions;
        return completions.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(partial))
                .collect(Collectors.toList());
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

    public static final class CommandMeta {
        public final List<ParamMeta> params;
        public final Method method;

        CommandMeta(List<ParamMeta> params, Method method) {
            this.params = params;
            this.method = method;
        }
    }

    public static final class ParamMeta {
        public final Parameter parameter;
        public final Class<?> type;
        public final boolean sender;
        public final boolean optional;
        public final String optionalDefault;
        public final boolean hidden;
        public final String name;
        public final String flag;
        public final boolean combined;

        ParamMeta(Parameter parameter) {
            this.parameter = parameter;
            this.type = parameter.getType();
            this.sender = parameter.isAnnotationPresent(Sender.class);
            dev.lorenzz.common.annotations.Optional opt = parameter.getAnnotation(dev.lorenzz.common.annotations.Optional.class);
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

    public static final class RootCommand {
        public final Object instance;
        public final MethodHandle handle;
        public final CommandMeta meta;
        public final Command ann;
        public final Permission permission;
        public final String name;
        public final String[] aliases;
        public final String usage;
        public final String description;
        public final Map<String, SubMeta> subcommands = new LinkedHashMap<>();

        RootCommand(Object instance, MethodHandle handle, CommandMeta meta, Command ann, Permission permission, String usage) {
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

        public void addSubcommand(SubMeta sub) {
            subcommands.put(sub.ann.name().toLowerCase(Locale.ROOT), sub);
        }
    }

    public static final class SubMeta {
        public final Object instance;
        public final MethodHandle handle;
        public final CommandMeta meta;
        public final Subcommand ann;
        public final Permission permission;
        public final String usage;

        SubMeta(Object instance, MethodHandle handle, CommandMeta meta, Subcommand ann, Permission permission, String usage) {
            this.instance = instance;
            this.handle = handle;
            this.meta = meta;
            this.ann = ann;
            this.permission = permission;
            this.usage = usage;
        }
    }
}
