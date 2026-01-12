package dev.lorenzz.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Hides an argument from dev.lorenzz.help and usage.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidden {
}