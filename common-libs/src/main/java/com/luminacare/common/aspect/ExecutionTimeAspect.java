package com.luminacare.common.aspect;

import com.luminacare.common.constants.AspectOrder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@Order(AspectOrder.EXECUTION_TIME)
@RequiredArgsConstructor
public class ExecutionTimeAspect {

    private final MeterRegistry meterRegistry;

    @Value("${app.aop.execution-time.slow-method-threshold-ms:500}")
    private long slowMethodThresholdMs;

    @Around("@within(org.springframework.stereotype.Service) || @within(org.springframework.web.bind.annotation.RestController)")
    public Object trackExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            
            sample.stop(Timer.builder("aspect.execution.time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .description("Execution time of service and controller methods")
                    .register(meterRegistry));

            if (executionTime > slowMethodThresholdMs) {
                log.warn("SLOW METHOD ALERT: {}.{} executed in {} ms (Threshold: {} ms)", 
                        className, methodName, executionTime, slowMethodThresholdMs);
            }
        }
    }
}
