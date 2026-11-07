package com.luminacare.common.constants;

/**
 * Centralized aspect ordering constants to ensure deterministic execution.
 * Lower values have higher priority.
 */
public final class AspectOrder {
    private AspectOrder() {
        // Prevent instantiation
    }

    public static final int ORGANIZATION = 1;
    public static final int LOGGING = 2;
    public static final int EXECUTION_TIME = 3;
    public static final int AUDIT = 4;
    public static final int SECURITY = 5;
    public static final int RETRY = 6;
    public static final int CACHE_INVALIDATION = 7;
}
