package dev.lorenzz.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a root commandapi.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String[] aliases() default {};
    boolean async() default true;
    String description() default "";
    String usage() default "";
}