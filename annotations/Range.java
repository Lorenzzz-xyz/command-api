package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares numeric range constraints.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
}