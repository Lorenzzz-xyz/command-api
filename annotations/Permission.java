package dev.lorenzz.commandapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares a permission requirement.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    String value();
}