package dev.lorenzz.common.help;

import dev.lorenzz.common.ColorTranslator;
import dev.lorenzz.common.LanguageProvider;
import dev.lorenzz.common.annotations.*;
import dev.lorenzz.common.exception.CommandExitException;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HelpGenerator {

    private HelpGenerator() {
    }

    public static String usage(String root, String sub, String customUsage, Parameter[] parameters) {
        if (customUsage != null && !customUsage.trim().isEmpty()) {
            return customUsage;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(root);

        if (sub != null && !sub.isEmpty()) {
            builder.append(" ").append(sub);
        }

        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Hidden.class)) continue;
            if (parameter.isAnnotationPresent(Sender.class)) continue;

            boolean optional = parameter.isAnnotationPresent(dev.lorenzz.common.annotations.Optional.class);
            Named named = parameter.getAnnotation(Named.class);
            Flag flag = parameter.getAnnotation(Flag.class);

            String name = flag != null ? flag.value() : named != null ? named.value() : parameter.getName();

            if (flag != null) {
                builder.append(" [").append(name).append("]");
            } else if (optional) {
                builder.append(" [").append(name).append("]");
            } else {
                builder.append(" <").append(name).append(">");
            }
        }

        return builder.toString();
    }

    public static PaginatedHelp subcommandHelp(String root, List<HelpEntry> entries,
                                                 LanguageProvider lang, ColorTranslator colors) {
        entries.sort(Comparator.comparing(HelpEntry::label));
        List<String> lines = new ArrayList<>();

        for (HelpEntry entry : entries) {
            boolean hasDesc = !entry.description().isEmpty()
                    && !entry.description().equalsIgnoreCase("N/A");

            if (hasDesc) {
                lines.add(colors.translate(lang.helpEntryDescription(root, entry.usage(), entry.description())));
            } else {
                lines.add(colors.translate(lang.helpEntryNoDescription(root, entry.usage())));
            }
        }

        return new PaginatedHelp(lines, 5);
    }

    public static void validateRanges(Parameter parameter, Object value, LanguageProvider lang,
                                       ColorTranslator colors) throws CommandExitException {
        Range range = parameter.getAnnotation(Range.class);
        if (range == null) return;

        if (value instanceof Number) {
            long v = ((Number) value).longValue();

            if (v < range.min()) {
                throw new CommandExitException(colors.translate(lang.inputTooLow(value)));
            }

            if (v > range.max()) {
                throw new CommandExitException(colors.translate(lang.inputTooHigh(value)));
            }
        }
    }

    public static class HelpEntry {
        private final String label;
        private final String usage;
        private final String description;

        public HelpEntry(String label, String usage, String description) {
            this.label = label;
            this.usage = usage;
            this.description = description;
        }

        public String label() { return label; }
        public String usage() { return usage; }
        public String description() { return description; }
    }
}
