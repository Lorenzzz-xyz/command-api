package dev.lorenzz.commandapi.provider;

import dev.lorenzz.commandapi.exception.CommandExitException;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface Provider<T> {
    T provide(String input) throws CommandExitException;
    List<String> tabComplete(CommandSender sender, String arg);
}