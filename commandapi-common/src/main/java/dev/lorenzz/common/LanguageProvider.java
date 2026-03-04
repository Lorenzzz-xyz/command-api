package dev.lorenzz.common;

public interface LanguageProvider {

    String noPermission();

    String inputTooLow(Object value);

    String inputTooHigh(Object value);

    String offlinePlayer(String name);

    String playerNeverJoined(String name);

    String invalidBoolean();

    String invalidNumber(String input);

    String invalidEnum(String input, String options);

    String playersOnly();

    String consolesOnly();

    String commandNotRegistered(String name);

    String errorOccurred();

    String helpHeader(String command, int page, int maxPages);

    String helpFooter(String command, int count);

    String helpEntryDescription(String command, String arguments, String description);

    String helpEntryNoDescription(String command, String arguments);
}
