package com.leetjourney.device_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.concurrent.TimeUnit;

@Aspect
@Slf4j
public class ExecutionTimeAspect {

    @Pointcut("execution(* com.leetjourney.device_service.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();

        try {
            return pjp.proceed();
        } finally {
            long end = System.nanoTime();
            long executionTime = end - start;

            long elapsedNs = TimeUnit.NANOSECONDS.toMillis(executionTime);
            String signature = pjp.getSignature().toShortString();
            log.info("Execution time of {}: {} ms", signature, elapsedNs);
        }
    }
}
