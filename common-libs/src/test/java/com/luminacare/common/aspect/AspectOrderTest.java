package com.luminacare.common.aspect;

import com.luminacare.common.constants.AspectOrder;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectOrderTest {

    @Test
    void verifyAspectExecutionOrderIsDeterministic() {
        // Enforce the required architecture rules:
        // Organization Context -> Logging (MDC) -> Metrics -> Audit -> Security -> Retry -> Cache

        int orgOrder = OrganizationAspect.class.getAnnotation(Order.class).value();
        int timeOrder = ExecutionTimeAspect.class.getAnnotation(Order.class).value();
        
        // Assert constants match expected architecture
        assertEquals(AspectOrder.ORGANIZATION, orgOrder);
        assertEquals(AspectOrder.EXECUTION_TIME, timeOrder);
        
        assertTrue(orgOrder < timeOrder, "OrganizationContext MUST be set before Execution metrics are collected");
        
        // The lower the @Order value, the higher the priority (executes first on the way in)
        assertTrue(AspectOrder.ORGANIZATION < AspectOrder.LOGGING);
        assertTrue(AspectOrder.LOGGING < AspectOrder.EXECUTION_TIME);
        assertTrue(AspectOrder.EXECUTION_TIME < AspectOrder.AUDIT);
        assertTrue(AspectOrder.AUDIT < AspectOrder.SECURITY);
        assertTrue(AspectOrder.SECURITY < AspectOrder.RETRY);
        assertTrue(AspectOrder.RETRY < AspectOrder.CACHE_INVALIDATION);
    }
}
