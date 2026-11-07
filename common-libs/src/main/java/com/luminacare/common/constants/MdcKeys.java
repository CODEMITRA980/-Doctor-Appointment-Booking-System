package com.luminacare.common.constants;

/**
 * Standard MDC keys for structured JSON logging.
 */
public final class MdcKeys {
    private MdcKeys() {
        // Prevent instantiation
    }

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String ORG_ID = "orgId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_ID = "requestId";
    public static final String ENDPOINT = "endpoint";
}
