package com.luminacare.common.util;

import com.luminacare.common.constants.Headers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Standardizes cache key generation to enforce multi-tenant isolation.
 */
@Component("cacheKeyBuilder")
public class CacheKeyBuilder {

    private static final String DEFAULT_ORG = "system";

    /**
     * Builds a key for a specific entity ID.
     * Format: {orgId}::{id}
     */
    public String build(Object id) {
        return getOrgId() + "::" + id.toString();
    }

    /**
     * Builds a key for a collection or specific query parameters.
     * Format: {orgId}::{param1}_{param2}
     */
    public String buildList(Object... params) {
        StringBuilder key = new StringBuilder(getOrgId()).append("::");
        for (int i = 0; i < params.length; i++) {
            key.append(params[i].toString());
            if (i < params.length - 1) {
                key.append("_");
            }
        }
        return key.toString();
    }

    /**
     * Builds a key for fetching all records for an organization.
     * Format: {orgId}::all
     */
    public String buildAll() {
        return getOrgId() + "::all";
    }

    private String getOrgId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String orgId = request.getHeader(Headers.X_ORG_ID);
            if (StringUtils.hasText(orgId)) {
                return orgId.trim();
            }
        }
        return DEFAULT_ORG; // Fallback, though requests should be rejected if missing in a multi-tenant setup
    }
}
