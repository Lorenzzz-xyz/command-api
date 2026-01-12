package dev.lorenzz.commandapi.exception;

public class CommandExitException extends Exception {
    public CommandExitException(String message) {
        super(message);
    }
}