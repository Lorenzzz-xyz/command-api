package dev.lorenzz.help;



import dev.lorenzz.annotations.*;
import dev.lorenzz.exception.CommandExitException;
import dev.lorenzz.util.CommandUtils;
import dev.lorenzz.util.LanguageLocale;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HelpGenerator {
    private HelpGenerator() {
    }

    public static String usage(String root, String sub, String usage, Parameter[] parameters) {
        if (usage != null && !usage.trim().isEmpty()) {
            return CommandUtils.colorize(usage);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(root);

        if (sub != null && !sub.isEmpty()) {
            builder.append(" ").append(sub);
        }

        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Hidden.class)) continue;
            if (parameter.isAnnotationPresent(Sender.class)) continue;

            boolean optional = parameter.isAnnotationPresent(Optional.class);
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

    public static PaginatedHelp subcommandHelp(String root, List<HelpEntry> entries) {
        entries.sort(Comparator.comparing(HelpEntry::label));
        List<String> lines = new ArrayList<>();

        for (HelpEntry entry : entries) {
            lines.add(CommandUtils.colorize(entry.description().isEmpty() || entry.description().equalsIgnoreCase("N/A") ?
                    LanguageLocale.HELP_COMMAND_ENTRY_NO_DESCRIPTION.getString()
                            .replace("<commandapi>", root)
                            .replace("<arguments>", entry.usage()) :
                    LanguageLocale.HELP_COMMAND_ENTRY_DESCRIPTION.getString()
                            .replace("<commandapi>", root)
                            .replace("<arguments>", entry.usage())
                            .replace("<description>", entry.description())));
            lines.add(CommandUtils.colorize("/" + root + entry.usage() + " - " + entry.description()));
        }

        return new PaginatedHelp(lines, 5);
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

        public String label() {
            return label;
        }

        public String usage() {
            return usage;
        }

        public String description() {
            return description;
        }
    }

    public static void validateRanges(Parameter parameter, Object value) throws CommandExitException {
        Range range = parameter.getAnnotation(Range.class);
        if (range == null) return;

        if (value instanceof Number) {
            long v = ((Number) value).longValue();

            if (v < range.min()) {
                throw new CommandExitException(CommandUtils.colorize(LanguageLocale.INPUT_LOWER_THAN_MIN.getFormattedString(value)));
            }

            if (v > range.max()) {
                throw new CommandExitException(CommandUtils.colorize(LanguageLocale.INPUT_HIGHER_THAN_MAX.getFormattedString(value)));
            }
        }
    }
}