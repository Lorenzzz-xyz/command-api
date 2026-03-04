package dev.lorenzz.common;

import java.util.Collection;

public interface CommandPlatform {

    void runAsync(Runnable task);

    Collection<String> getOnlinePlayerNames();

    ColorTranslator getColorTranslator();

    LanguageProvider getLanguageProvider();
}
