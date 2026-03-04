package dev.lorenzz.common;

public class DefaultLanguageProvider implements LanguageProvider {

    @Override
    public String noPermission() {
        return "&5&lCORE &8&l» &cNessun permesso!";
    }

    @Override
    public String inputTooLow(Object value) {
        return "&5&lCORE &8&l» &cErrore: devi inserire un valore maggiore di " + value + "!";
    }

    @Override
    public String inputTooHigh(Object value) {
        return "&5&lCORE &8&l» &cErrore: devi inserire un valore minore di " + value + "!";
    }

    @Override
    public String offlinePlayer(String name) {
        return "&5&lCORE &8&l» &cErrore: " + name + " non è online!";
    }

    @Override
    public String playerNeverJoined(String name) {
        return "&5&lCORE &8&l» &cErrore: " + name + " non è mai entrato nel server!";
    }

    @Override
    public String invalidBoolean() {
        return "&5&lDEVELOPER &8&l» &cErrore: non è un booleano valido!";
    }

    @Override
    public String invalidNumber(String input) {
        return "&5&lDEVELOPER &8&l» &cErrore: " + input + " non è un numero valido!";
    }

    @Override
    public String invalidEnum(String input, String options) {
        return "&5&lCORE &8&l» &cErrore: " + input + " non è un'opzione valida! Opzioni disponibili: " + options;
    }

    @Override
    public String playersOnly() {
        return "&5&lCORE &8&l» &cErrore: questo comando può essere eseguito solo da un giocatore!";
    }

    @Override
    public String consolesOnly() {
        return "&5&lCORE &8&l» &cErrore: questo comando può essere usato solo dalla console!";
    }

    @Override
    public String commandNotRegistered(String name) {
        return "&c&lBUG &8&l» &cErrore: il comando /" + name + " non è registrato correttamente!";
    }

    @Override
    public String errorOccurred() {
        return "&5&lCORE &8&l» &cErrore: si è verificato un errore imprevisto!";
    }

    @Override
    public String helpHeader(String command, int page, int maxPages) {
        return "&ePagina aiuto &d(/" + command + ") &7- &b(" + page + "/" + maxPages + ")\n&r";
    }

    @Override
    public String helpFooter(String command, int count) {
        return "&r\n&7Trovati (" + count + ") sottocomandi per (/" + command + ")";
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
