package com.luminacare.common.interceptor;

import com.luminacare.common.constants.Headers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TenantGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String orgId = request.getHeader(Headers.X_ORG_ID);
        
        // Exclude actuator health checks and openapi docs from strict tenant requirements
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.contains("/auth/login")) {
            return true;
        }

        if (!StringUtils.hasText(orgId)) {
            log.error("SECURITY ALERT: Request to {} rejected. Missing {}", path, Headers.X_ORG_ID);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        
        // Whitelist / regex validation
        if (!orgId.matches("^[a-zA-Z0-9-]{3,36}$")) {
            log.error("SECURITY ALERT: Invalid tenant ID format: {}", orgId);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }

        return true;
    }
}
