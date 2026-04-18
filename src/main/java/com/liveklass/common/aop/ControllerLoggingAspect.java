package com.liveklass.common.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

	private static final long SLOW_THRESHOLD_MS = 500L;

	@Around("execution( * com.liveklass..controller..*(..) )")
	public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime();

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();
		Object[] args = joinPoint.getArgs();

		log.debug("[Controller Request] {}.{} args = {}", className, methodName, Arrays.toString(args));

		try {
			Object result = joinPoint.proceed();
			long executionTime = (System.nanoTime() - start) / 1_000_000;

			log.info("[Controller Response] {}.{} time = {}ms", className, methodName, executionTime);

			if (executionTime >= SLOW_THRESHOLD_MS) {
				log.warn("[Slow Controller] {}.{} executed in {}ms", className, methodName, executionTime);
			}

			return result;
		} catch (Exception e) {
			long executionTime = (System.nanoTime() - start) / 1_000_000;

			log.error("[Controller Exception] {}.{} time = {}ms, message = {}",
				className, methodName, executionTime, e.getMessage(), e);

			throw e;
		}
	}

}
