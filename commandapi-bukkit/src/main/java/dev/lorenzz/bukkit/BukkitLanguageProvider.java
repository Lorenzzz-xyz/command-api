package dev.lorenzz.bukkit;

import dev.lorenzz.common.DefaultLanguageProvider;
import dev.lorenzz.common.LanguageProvider;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public final class BukkitLanguageProvider implements LanguageProvider {

    private final DefaultLanguageProvider defaults = new DefaultLanguageProvider();
    private String noPermission;
    private String inputTooLow;
    private String inputTooHigh;
    private String offlinePlayer;
    private String playerNeverJoined;
    private String invalidBoolean;
    private String invalidNumber;
    private String invalidEnum;
    private String playersOnly;
    private String consolesOnly;
    private String commandNotRegistered;
    private String errorOccurred;
    private String helpHeader;
    private String helpFooter;
    private String helpEntryDescription;
    private String helpEntryNoDescription;

    @SneakyThrows
    public BukkitLanguageProvider(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "commands-language.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        noPermission = loadOrSet(config, "no-permission", defaults.noPermission());
        inputTooLow = loadOrSet(config, "input-too-low", defaults.inputTooLow("%s"));
        inputTooHigh = loadOrSet(config, "input-too-high", defaults.inputTooHigh("%s"));
        offlinePlayer = loadOrSet(config, "offline-player", defaults.offlinePlayer("%s"));
        playerNeverJoined = loadOrSet(config, "player-never-joined", defaults.playerNeverJoined("%s"));
        invalidBoolean = loadOrSet(config, "invalid-boolean", defaults.invalidBoolean());
        invalidNumber = loadOrSet(config, "invalid-integer", defaults.invalidNumber("%s"));
        invalidEnum = loadOrSet(config, "invalid-option", defaults.invalidEnum("%s", "%s"));
        playersOnly = loadOrSet(config, "players-only", defaults.playersOnly());
        consolesOnly = loadOrSet(config, "consoles-only", defaults.consolesOnly());
        commandNotRegistered = loadOrSet(config, "command-not-registered", defaults.commandNotRegistered("%s"));
        errorOccurred = loadOrSet(config, "error-occurred", defaults.errorOccurred());
        helpHeader = loadOrSet(config, "help-header", "&eHelp &d(/<command>) &7- &b(<page>/<max>)\n&r");
        helpFooter = loadOrSet(config, "help-footer", "&r\n&7Found (<count>) subcommands for (/<command>)");
        helpEntryDescription = loadOrSet(config, "help-entry-description", " &7- &e/<command> &d<arguments> &7- &f<description>");
        helpEntryNoDescription = loadOrSet(config, "help-entry-no-description", " &7- &e/<command> &d<arguments>");

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
        }
        config.save(file);
    }

    private String loadOrSet(YamlConfiguration config, String path, String defaultValue) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            return defaultValue;
        }
        return config.getString(path, defaultValue);
    }

    @Override public String noPermission() { return noPermission; }
    @Override public String inputTooLow(Object value) { return String.format(inputTooLow, value); }
    @Override public String inputTooHigh(Object value) { return String.format(inputTooHigh, value); }
    @Override public String offlinePlayer(String name) { return String.format(offlinePlayer, name); }
    @Override public String playerNeverJoined(String name) { return String.format(playerNeverJoined, name); }
    @Override public String invalidBoolean() { return invalidBoolean; }
    @Override public String invalidNumber(String input) { return String.format(invalidNumber, input); }
    @Override public String invalidEnum(String input, String options) { return String.format(invalidEnum, input, options); }
    @Override public String playersOnly() { return playersOnly; }
    @Override public String consolesOnly() { return consolesOnly; }
    @Override public String commandNotRegistered(String name) { return String.format(commandNotRegistered, name); }
    @Override public String errorOccurred() { return errorOccurred; }

    @Override
    public String helpHeader(String command, int page, int maxPages) {
        return helpHeader.replace("<command>", command)
                .replace("<page>", String.valueOf(page))
                .replace("<max>", String.valueOf(maxPages));
    }

    @Override
    public String helpFooter(String command, int count) {
        return helpFooter.replace("<command>", command)
                .replace("<count>", String.valueOf(count));
    }

    @Override
    public String helpEntryDescription(String command, String arguments, String description) {
        return helpEntryDescription.replace("<command>", command)
                .replace("<arguments>", arguments)
                .replace("<description>", description);
    }

    @Override
    public String helpEntryNoDescription(String command, String arguments) {
        return helpEntryNoDescription.replace("<command>", command)
                .replace("<arguments>", arguments);
    }
}
