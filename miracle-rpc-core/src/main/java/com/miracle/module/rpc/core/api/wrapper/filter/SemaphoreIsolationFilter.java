package com.miracle.module.rpc.core.api.wrapper.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;

@Activate(group = Constants.PROVIDER_SIDE, providerorder = Integer.MIN_VALUE+2000)
public class SemaphoreIsolationFilter implements Filter {

	private final ConcurrentMap<String, AtomicInteger> METHOD_CONCURRENTS = 
			new ConcurrentHashMap<String, AtomicInteger>();

	 private AtomicInteger getMethodConcurrent(RpcRequest request) {
        String key = request.getInterfaceName() + "." + request.getMethodName();
        AtomicInteger concurrent = METHOD_CONCURRENTS.get(key);
        if (concurrent == null) {
        	METHOD_CONCURRENTS.putIfAbsent(key, new AtomicInteger());
            concurrent = METHOD_CONCURRENTS.get(key);
        }
        return concurrent;
    }
    
    
    private int getInvConcurrent(RpcRequest request)
    {
    	if(request == null) {
			return 0;
		}
    	return getMethodConcurrent(request).get();
    }
    
    private void incConcurrent(RpcRequest request)
    {
    	if(request != null)
    	{
    		getMethodConcurrent(request).incrementAndGet();
    	}
    }
    private void decConcurrent(RpcRequest request)
    {
    	if(request != null)
    	{
    		getMethodConcurrent(request).decrementAndGet();
    	}
    }
    
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		int semaphoreConcurrent = invoker.getConfig().getMethodParameter(request.getMethodName(), 
				Constants.SEMAPHORE_CONCURRENT_KEY, Constants.DEFAULT_SEMAPHORE_CONCURRENT);
		if(getInvConcurrent(request) > semaphoreConcurrent)
		{
			boolean isProviderSide = invoker.getConfig().isProvider();
			RpcMetricCollector.getInstance().markSemaphoreRejected(request, isProviderSide);
			throw new RpcException(RpcException.NOSEMAPHORE_EXCEPTION,
	                    new StringBuilder(64)
	                            .append("Failed to invoke service ")
	                            .append(invoker.getInterface().getName())
	                            .append(".")
	                            .append(request.getMethodName())
	                            .append(" because exceed max semaphore concurrent.")
	                            .toString());
		}
		try{
			incConcurrent(request);
			return invoker.invoke(request);
		}
		finally{
			decConcurrent(request);
		}
	}

}
