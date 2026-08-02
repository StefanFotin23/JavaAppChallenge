package com.example.app.ekyc.shared.profiling;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EkycProfilingAspect {

    private final ProfilingRegistry profilingRegistry;

    @Pointcut("within(com.example.app.ekyc.controller..*) || within(com.example.app.ekyc.shared.service..*)")
    public void ekycTargetPackages() {}

    @Around("ekycTargetPackages()")
    public Object profileMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationNs = System.nanoTime() - start;
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String fullIdentifier = className + "." + methodName + "()";
            profilingRegistry.recordExecution(fullIdentifier, durationNs);
        }
    }
}
