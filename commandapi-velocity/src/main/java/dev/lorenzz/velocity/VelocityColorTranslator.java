package dev.lorenzz.velocity;

import dev.lorenzz.common.ColorTranslator;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityColorTranslator implements ColorTranslator {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .hexCharacter('#')
            .build();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

    @Override
    public String translate(String input) {
        if (input == null) return "";
        return LEGACY_SECTION.serialize(LEGACY_AMPERSAND.deserialize(input));
    }
}
