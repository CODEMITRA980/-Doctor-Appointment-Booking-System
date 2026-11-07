package com.luminacare.common.annotation;

import java.lang.annotation.*;

/**
 * Validates that the current user context contains at least one of the specified roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
}
