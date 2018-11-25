package com.miracle.module.rpc.core.api;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.google.common.base.Defaults;

public class InvokerInvocationHandler implements InvocationHandler {

    private final Invoker<?> invoker;
    
    public InvokerInvocationHandler(Invoker<?> handler){
        this.invoker = handler;
    }

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        
        Object retObj = invoker.invoke(new RpcRequest(invoker.getInterface(), method, args)).recreate();
        
        //fix null pointer exception when null converted to java primitive type in async invoker mode
        Class<?> retType = method.getReturnType();
        if(retObj == null && retType.isPrimitive())
        {
        	retObj = Defaults.defaultValue(retType);
        }

		return retObj;
	}

}