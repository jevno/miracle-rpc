package com.miracle.module.rpc.core.api.coordinator;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;

import com.google.common.collect.Lists;

public class ProxyCoordinatorInterceptorManager {
	
	private static ProxyCoordinatorInterceptorManager instance = new ProxyCoordinatorInterceptorManager();
	
	List<ProxyCoordinatorInterceptor> interceptorList = Lists.newCopyOnWriteArrayList();
	
	private InterceptorInvoker interceptedInvokerChain = null;
	
	private ProxyCoordinatorInterceptorManager()
	{	
	}
	
	public static ProxyCoordinatorInterceptorManager getInstance()
	{
		return instance;
	}
	
	public void registerProxyCoordinatorInterceptor(ProxyCoordinatorInterceptor interceptor)
	{
		if(!interceptorList.contains(interceptor))
		{
			interceptorList.add(interceptor);
		}
	}
	
	
	public InterceptorInvoker getInterceptedInvokerChain()
	{
		if(interceptedInvokerChain == null)
		{
			synchronized(this)
			{
				if(interceptedInvokerChain == null)
				{
					InterceptorInvoker last = new InterceptorInvoker(){

						@Override
						public Object invoke(ProceedingJoinPoint pjp)
								throws Throwable {
							return pjp.proceed();
						}
						
					};
					
					if(interceptorList != null)
					{
						for(ProxyCoordinatorInterceptor interceptor : interceptorList)
						{
							final ProxyCoordinatorInterceptor curInterceptor = interceptor;
							final InterceptorInvoker next = last;
							
							last = new InterceptorInvoker()
							{

								@Override
								public Object invoke(
										ProceedingJoinPoint pjp) throws Throwable {
									return curInterceptor.interceptProxyCoordinatorMethod(next, pjp);
								}
							};
						}
					}
					interceptedInvokerChain = last;
				}
			}
		}
		return interceptedInvokerChain;
	}
}
