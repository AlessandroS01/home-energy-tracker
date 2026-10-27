package com.leetjourney.device_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.leetjourney.device_service.service.*.*(..))")
    public void serviceMethod() {}

    @Before("serviceMethod()")
    public void logBefore(JoinPoint jp) {
        log.info("Executing service method: {} with arguments: {}",
                jp.getSignature().toShortString(), jp.getArgs());
    }

    @AfterReturning(pointcut = "serviceMethod()", returning = "result")
    public void logAfter(Object result) {
        log.info("Service method result: {}", result);
    }
}
