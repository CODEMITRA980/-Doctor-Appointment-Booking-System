package com.luminacare.common.aspect;

import com.luminacare.common.constants.AspectOrder;
import com.luminacare.common.constants.Headers;
import com.luminacare.common.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@Order(AspectOrder.ORGANIZATION)
@RequiredArgsConstructor
public class OrganizationAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object manageTenantContext(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String orgId = request.getHeader(Headers.X_ORG_ID);
                
                if (StringUtils.hasText(orgId)) {
                    TenantContext.setTenantId(orgId.trim());
                } else {
                    TenantContext.setTenantId("system");
                }
            } else {
                TenantContext.setTenantId("system"); // Fallback for async/scheduled internal calls
            }
            
            return joinPoint.proceed();
        } finally {
            // CRITICAL: Prevent tenant bleed across thread pool reuse
            TenantContext.clear();
        }
    }
}
