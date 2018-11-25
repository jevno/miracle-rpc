package com.miracle.module.rpc.core.api;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.coordinator.CoordinatorInvokerInvocationHandler;


public class CglibProxyFactory {
	private static Logger log = Logger.getLogger(CglibProxyFactory.class);
	
	private static Map<String, Method> cachedNamedMethod = new ConcurrentHashMap<>();
	private static Map<String, FastMethod> cachedFastMethod = new ConcurrentHashMap<>();
	
	public static Method getOnlyMethodByName(Class<?> serviceClass, String methodName)
	{
		String key = serviceClass.getName() + methodName;
		Method method = cachedNamedMethod.get(key);
		if(method != null)
		{
			return method;
		}
		
		List<Method> candidateMethodList = new ArrayList<Method>();
		Method[] methodArr = serviceClass.getMethods();
		if(methodArr != null && methodArr.length > 0)
		{
			for(Method m : methodArr)
			{
				if(m.getName().equals(methodName))
				{
					candidateMethodList.add(m);
				}
			}
		}
		if(candidateMethodList.size() > 1)
		{
			log.fatal("Two or more method named as "+methodName+" in service class " + serviceClass.getName());
		}
		else if(candidateMethodList.size() <= 0)
		{
			log.fatal("No method named as "+methodName+" in service class " + serviceClass.getName());
		}
		else
		{
            Method serviceMethod = candidateMethodList.get(0);
            cachedNamedMethod.put(key, serviceMethod);
			return serviceMethod;
		}
		return null;
	}
	
	public static FastMethod getFastMathodByName(Class<?> serviceClass, String methodName)
	{
		String key = serviceClass.getName() + methodName;
		FastMethod fastMethod = cachedFastMethod.get(key);
		if(fastMethod != null)
		{
			return fastMethod;
		}
		
		Method method = getOnlyMethodByName(serviceClass, methodName);
		if(method != null)
		{
			FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(method);
            cachedFastMethod.put(key, serviceFastMethod);
			return serviceFastMethod;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
		Class<?> interfaceClass = invoker.getClass();
		ClassLoader cl = interfaceClass.getClassLoader();
	    T proxy = (T) Proxy.newProxyInstance(cl, interfaces, new InvokerInvocationHandler(invoker));
	    
	    T coordinatorProxy = (T) Proxy.newProxyInstance(cl, interfaces, 
	    		new CoordinatorInvokerInvocationHandler(proxy, invoker, invoker.getInterface()));
	    
	    return coordinatorProxy;
	}
	
	public static <T> T getProxy(Invoker<T> invoker) throws RpcException {
	     Class<?>[] interfaces = new Class<?>[] {invoker.getInterface()};
	     return getProxy(invoker, interfaces);
	}
	
	public static <T> Invoker<T> getInvoker(T proxy, Class<T> type, RpcConfig config) {

        return new AbstractProxyInvoker<T>(proxy, type, config) {
            @Override
            protected Object doInvoke(T proxy, String methodName, 
                                      Object[] arguments) throws Throwable {
            	 Class<?> serviceClass = proxy.getClass();
            	 FastMethod fastMethod = getFastMathodByName(serviceClass, methodName);
            	 if(fastMethod == null)
            	 {
            		 throw new RpcException(RpcException.NONEORMORE_METHOD_EXCEPTION, "No or more than one service method defined in class " 
            				 + serviceClass.getName() + " : " + methodName);
            	 }
                 return fastMethod.invoke(proxy, arguments);
            }
        };
    }
}
