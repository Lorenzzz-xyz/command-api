package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares a boolean flag argument.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {
    String value();
}