package com.luminacare.common.annotation;

import java.lang.annotation.*;

/**
 * Marks a method for asynchronous audit logging.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String action();
    String entity();
}
