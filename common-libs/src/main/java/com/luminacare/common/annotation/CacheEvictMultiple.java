package com.luminacare.common.annotation;

import java.lang.annotation.*;

/**
 * Marks a method to evict multiple specific caches and keys when invoked.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvictMultiple {
    String[] cacheNames();
    String[] keyPatterns() default {"*"};
}
