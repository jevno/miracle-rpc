package com.miracle.module.rpc.core.api.coordinator;

import org.aspectj.lang.ProceedingJoinPoint;

public interface ProxyCoordinatorInterceptor {
	
	Object interceptProxyCoordinatorMethod(InterceptorInvoker invoker, ProceedingJoinPoint pjp) throws Throwable ;
	
}
