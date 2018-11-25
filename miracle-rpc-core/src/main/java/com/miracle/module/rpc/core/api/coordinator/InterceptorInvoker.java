package com.miracle.module.rpc.core.api.coordinator;

import org.aspectj.lang.ProceedingJoinPoint;

public interface InterceptorInvoker {
	
	Object invoke(ProceedingJoinPoint pjp) throws Throwable;
}
