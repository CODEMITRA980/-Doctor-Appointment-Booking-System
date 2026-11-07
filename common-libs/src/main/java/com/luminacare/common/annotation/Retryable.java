package com.luminacare.common.annotation;

import java.lang.annotation.*;

/**
 * Marks a method for retry in case of specific transient exceptions.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {
    int maxAttempts() default 2;
    long backoffMs() default 1000;
    Class<? extends Throwable>[] retryOn() default {Exception.class};
}
