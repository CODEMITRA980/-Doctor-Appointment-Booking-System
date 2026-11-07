package com.luminacare.common.annotation;

import org.slf4j.event.Level;
import java.lang.annotation.*;

/**
 * Enables structured logging of method entry, exit, and execution time.
 * Supports masking PII data in arguments.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {
    Level level() default Level.INFO;
    boolean includeArgs() default true;
    String[] maskFields() default {};
}
