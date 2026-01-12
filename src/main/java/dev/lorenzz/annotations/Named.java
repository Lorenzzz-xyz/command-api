package dev.lorenzz.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Names an argument for usage and dev.lorenzz.help.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Named {
    String value();
}