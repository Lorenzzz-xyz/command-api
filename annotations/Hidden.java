package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Hides an argument from help and usage.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidden {
}