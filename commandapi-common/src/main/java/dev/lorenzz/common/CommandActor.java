package dev.lorenzz.common;

public interface CommandActor {

    void sendMessage(String message);

    boolean hasPermission(String permission);

    boolean isConsole();

    boolean isPlayer();

    String getName();

    <T> T unwrap(Class<T> type);
}
