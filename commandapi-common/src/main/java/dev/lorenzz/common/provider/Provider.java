package dev.lorenzz.common.provider;

import dev.lorenzz.common.CommandActor;
import dev.lorenzz.common.exception.CommandExitException;

import java.util.List;

public interface Provider<T> {

    T provide(String input) throws CommandExitException;

    List<String> tabComplete(CommandActor actor, String arg);
}
