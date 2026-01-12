package dev.lorenzz.exception;

public class CommandExitException extends Exception {
    public CommandExitException(String message) {
        super(message);
    }
}