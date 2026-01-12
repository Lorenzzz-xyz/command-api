package dev.lorenzz.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks an argument to consume all remaining input as a single string.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Combined {
}