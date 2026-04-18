package com.liveklass.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

	private static final long SLOW_THRESHOLD_MS = 1000L;

	@Around("execution( * com.liveklass..service..*(..) )")
	public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime();

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();

		log.debug("[Service Start] {}.{}", className, methodName);

		Throwable caught = null;
		Object result = null;

		try {
			result = joinPoint.proceed();
		} catch (Throwable t) {
			caught = t;
		} finally {
			long executionTime = (System.nanoTime() - start) / 1_000_000;
			if (caught != null) {
				log.error("[Service Exception] {}.{} time = {}ms, message = {}",
					className, methodName, executionTime, caught.getMessage(), caught);
			} else {
				log.debug("[Service End] {}.{} time = {}ms", className, methodName, executionTime);
				if (executionTime >= SLOW_THRESHOLD_MS) {
					log.warn("[Slow Service] {}.{} executed in {}ms", className, methodName, executionTime);
				}
			}
		}

		if (caught != null) {
			throw caught;
		}
		return result;
	}
}
