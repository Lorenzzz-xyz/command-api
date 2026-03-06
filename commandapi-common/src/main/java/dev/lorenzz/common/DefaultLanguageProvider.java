package dev.lorenzz.common;

public class DefaultLanguageProvider implements LanguageProvider {

    @Override
    public String noPermission() {
        return "&cYou don't have permission!";
    }

    @Override
    public String inputTooLow(Object value) {
        return "&cValue must be greater than " + value + "!";
    }

    @Override
    public String inputTooHigh(Object value) {
        return "&cValue must be less than " + value + "!";
    }

    @Override
    public String offlinePlayer(String name) {
        return "&c" + name + " is not online!";
    }

    @Override
    public String playerNeverJoined(String name) {
        return "&c" + name + " has never joined the server!";
    }

    @Override
    public String invalidBoolean() {
        return "&cInvalid boolean value!";
    }

    @Override
    public String invalidNumber(String input) {
        return "&c" + input + " is not a valid number!";
    }

    @Override
    public String invalidEnum(String input, String options) {
        return "&c" + input + " is not a valid option! Available: " + options;
    }

    @Override
    public String playersOnly() {
        return "&cThis command can only be executed by a player!";
    }

    @Override
    public String consolesOnly() {
        return "&cThis command can only be executed from console!";
    }

    @Override
    public String commandNotRegistered(String name) {
        return "&cCommand /" + name + " is not registered correctly!";
    }

    @Override
    public String errorOccurred() {
        return "&cAn unexpected error occurred!";
    }

    @Override
    public String helpHeader(String command, int page, int maxPages) {
        return "&eHelp &d(/" + command + ") &7- &b(" + page + "/" + maxPages + ")\n&r";
    }

    @Override
    public String helpFooter(String command, int count) {
        return "&r\n&7Found (" + count + ") subcommands for (/" + command + ")";
    }

    @Override
    public String helpEntryDescription(String command, String arguments, String description) {
        return " &7- &e/" + command + " &d" + arguments + " &7- &f" + description;
    }

    @Override
    public String helpEntryNoDescription(String command, String arguments) {
        return " &7- &e/" + command + " &d" + arguments;
    }
}
