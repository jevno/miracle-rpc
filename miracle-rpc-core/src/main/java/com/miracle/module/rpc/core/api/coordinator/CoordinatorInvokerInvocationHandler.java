package com.miracle.module.rpc.core.api.coordinator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.InvokerInvocationHandler;

public class CoordinatorInvokerInvocationHandler extends InvokerInvocationHandler {

	private Object target;
	
	private Class<?> interfaceClass;
	
	public CoordinatorInvokerInvocationHandler(Invoker<?> handler) {
		super(handler);
	}

	public <T> CoordinatorInvokerInvocationHandler(T target, Invoker<T> invoker, Class<?> interfaceClass) {
        super(invoker);
        this.target = target;
        this.interfaceClass = interfaceClass;
    }

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Annotation[] annotations = method.getAnnotations();
		if(annotations != null && annotations.length > 0)
		{
			MethodProceedingJoinPoint pjp = new MethodProceedingJoinPoint(proxy, target, this.interfaceClass, method, args);
			InterceptorInvoker interceptedInvokerChain = 
					ProxyCoordinatorInterceptorManager.getInstance().getInterceptedInvokerChain();
			return interceptedInvokerChain.invoke(pjp);
		}
				
		return super.invoke(target, method, args);
	}
	
}
