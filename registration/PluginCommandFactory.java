package dev.lorenzz.commandapi.registration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class PluginCommandFactory {

    @FunctionalInterface
    interface Executor {
        boolean execute(CommandSender sender, String label, String[] args);
    }

    @FunctionalInterface
    interface Completer {
        List<String> complete(CommandSender sender, String label, String[] args);
    }

    static final class DynamicCommand extends Command {
        private final Executor executor;
        private final Completer completer;

        DynamicCommand(String name, List<String> aliases, Executor executor, Completer completer) {
            super(name);

            this.setAliases(aliases);
            this.executor = executor;
            this.completer = completer;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (executor == null) return false;

            return executor.execute(sender, commandLabel, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (completer == null) return Collections.emptyList();

            List<String> result = completer.complete(sender, alias, args);
            return result == null ? Collections.emptyList() : result;
        }
    }

    public Command create(String name, org.bukkit.plugin.Plugin plugin, String[] aliases, Executor executor, Completer completer) {
        return new DynamicCommand(name, Arrays.asList(aliases), executor, completer);
    }
}