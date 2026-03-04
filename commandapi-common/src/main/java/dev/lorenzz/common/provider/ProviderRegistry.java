package dev.lorenzz.common.provider;

import dev.lorenzz.common.CommandActor;
import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.exception.CommandExitException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ProviderRegistry {

    private final Map<Class<?>, Provider<?>> registry = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, Provider<T> provider) {
        registry.put(type, provider);
    }

    @SuppressWarnings("unchecked")
    public <T> Provider<T> get(Class<T> type) {
        Provider<?> provider = registry.get(type);

        if (provider == null && type.isEnum()) {
            provider = createEnumProvider(type);
            registry.put(type, provider);
        }

        return (Provider<T>) provider;
    }

    public void registerDefaults(LanguageProvider lang, ColorTranslator colors) {
        register(String.class, new Provider<String>() {
            @Override
            public String provide(String input) {
                return input;
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                return Collections.emptyList();
            }
        });

        register(Integer.class, numericProvider(Integer::valueOf, lang, colors));
        register(int.class, get(Integer.class));

        register(Long.class, numericProvider(Long::valueOf, lang, colors));
        register(long.class, get(Long.class));

        register(Double.class, numericProvider(Double::valueOf, lang, colors));
        register(double.class, get(Double.class));

        register(Float.class, numericProvider(Float::valueOf, lang, colors));
        register(float.class, get(Float.class));

        register(Boolean.class, new Provider<Boolean>() {
            @Override
            public Boolean provide(String input) throws CommandExitException {
                if (input == null) throw new CommandExitException("Missing boolean");
                if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes")) return true;
                if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no")) return false;
                throw new CommandExitException(colors.translate(lang.invalidBoolean()));
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                return Arrays.asList("true", "false");
            }
        });
        register(boolean.class, get(Boolean.class));
    }

    private <N> Provider<N> numericProvider(NumericParser<N> parser, LanguageProvider lang, ColorTranslator colors) {
        return new Provider<N>() {
            @Override
            public N provide(String input) throws CommandExitException {
                try {
                    return parser.parse(input);
                } catch (Exception ex) {
                    throw new CommandExitException(colors.translate(lang.invalidNumber(input)));
                }
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                return Collections.emptyList();
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Provider<?> createEnumProvider(Class<?> type) {
        Object[] constants = type.getEnumConstants();

        return new Provider<Object>() {
            @Override
            public Object provide(String input) throws CommandExitException {
                for (Object constant : constants) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(input)) {
                        return constant;
                    }
                }
                String options = Arrays.stream(constants)
                        .map(c -> ((Enum<?>) c).name().toLowerCase())
                        .collect(Collectors.joining(", "));
                throw new CommandExitException(input + " is not valid. Options: " + options);
            }

            @Override
            public List<String> tabComplete(CommandActor actor, String arg) {
                List<String> list = new ArrayList<>();
                for (Object constant : constants) {
                    String name = ((Enum<?>) constant).name().toLowerCase(Locale.ROOT);
                    if (arg.isEmpty() || name.startsWith(arg.toLowerCase(Locale.ROOT))) {
                        list.add(name);
                    }
                }
                return list;
            }
        };
    }

    @FunctionalInterface
    private interface NumericParser<N> {
        N parse(String input) throws Exception;
    }
}
