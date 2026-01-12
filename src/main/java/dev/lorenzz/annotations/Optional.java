package dev.lorenzz.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks an optional argument with an optional default value.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
    String value() default "";
}