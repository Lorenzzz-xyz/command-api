package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Names an argument for usage and help.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Named {
    String value();
}