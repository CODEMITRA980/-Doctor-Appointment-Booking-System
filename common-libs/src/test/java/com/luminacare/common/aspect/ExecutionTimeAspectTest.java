package com.luminacare.common.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionTimeAspectTest {

    private ExecutionTimeAspect executionTimeAspect;
    private MeterRegistry meterRegistry;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        executionTimeAspect = new ExecutionTimeAspect(meterRegistry);
        ReflectionTestUtils.setField(executionTimeAspect, "slowMethodThresholdMs", 100L);
    }

    @Test
    void shouldRecordExecutionTimeInMetrics() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(this.getClass());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(10);
            return "SUCCESS";
        });

        Object result = executionTimeAspect.trackExecutionTime(joinPoint);

        assertEquals("SUCCESS", result);
        verify(joinPoint, times(1)).proceed();

        // Verify micrometer registered the timer
        assertNotNull(meterRegistry.find("aspect.execution.time").timer());
        assertEquals(1, meterRegistry.find("aspect.execution.time").timer().count());
    }
}
