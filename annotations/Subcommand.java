package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a subcommand of a root commandapi.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Subcommand {
    String name();
    boolean async() default false;
    String description() default "";
}