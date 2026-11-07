package com.luminacare.common.aspect;

import com.luminacare.common.constants.Headers;
import com.luminacare.common.context.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationAspectTest {

    @InjectMocks
    private OrganizationAspect organizationAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        TenantContext.clear();
    }

    @Test
    void shouldExtractTenantIdFromHeader() throws Throwable {
        request.addHeader(Headers.X_ORG_ID, "org-123");
        
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertEquals("org-123", TenantContext.getTenantId());
            return "SUCCESS";
        });

        Object result = organizationAspect.manageTenantContext(joinPoint);
        
        assertEquals("SUCCESS", result);
        assertNull(TenantContext.getTenantId(), "TenantContext must be cleared after execution");
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void shouldFallbackToSystemWhenHeaderIsMissing() throws Throwable {
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertEquals("system", TenantContext.getTenantId());
            return "SUCCESS";
        });

        organizationAspect.manageTenantContext(joinPoint);
        
        assertNull(TenantContext.getTenantId(), "TenantContext must be cleared after execution");
    }

    @Test
    void shouldClearContextEvenIfExceptionThrown() throws Throwable {
        request.addHeader(Headers.X_ORG_ID, "org-123");
        
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business error"));

        try {
            organizationAspect.manageTenantContext(joinPoint);
        } catch (RuntimeException ignored) {
        }
        
        assertNull(TenantContext.getTenantId(), "TenantContext must be cleared even on exception");
    }
}
