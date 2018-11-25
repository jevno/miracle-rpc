package com.miracle.module.rpc.core.api.wrapper.filter;

import java.util.concurrent.ConcurrentHashMap;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.circuitbreaker.CircuitBreaker;
import com.miracle.module.rpc.core.api.wrapper.filter.circuitbreaker.CircuitBreakerConfig;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;

@Activate(group = Constants.CONSUMER_SIDE, consumerorder = Integer.MAX_VALUE-100000)
public class CircuitBreakerFilter implements Filter {

	private final ConcurrentHashMap<String, CircuitBreaker> CIRCUIT_BREAKERS = new ConcurrentHashMap<String, CircuitBreaker>();
	
	private String getKey(RpcRequest request)
	{
		return request.getInterfaceName() + "." + request.getMethodName();
	}
	
	public CircuitBreaker getCircuitBreaker(RpcRequest request)
	{
		String key = getKey(request);
		return CIRCUIT_BREAKERS.get(key);
	}
	
	
	private CircuitBreaker getOrRegisterCircuitBreaker(RpcConfig config, RpcRequest request)
	{
		String key = getKey(request);
		String methodName = request.getMethodName();
		CircuitBreaker breaker = CIRCUIT_BREAKERS.get(key);
		if(breaker == null)
		{
			 int failThreshold = config.getMethodParameter(methodName, Constants.CB_FAIL_THRESHOLD_KEY, 
					 					Constants.DEFAULT_CB_FAIL_THRESHOLD);
			 int volumeThreshold = config.getMethodParameter(methodName, Constants.CB_VOLUME_THRESHOLD_KEY,
	                                    Constants.DEFAULT_CB_VOLUME_THRESHOLD);
			 int halfOpenTimeout = config.getMethodParameter(methodName, Constants.CB_HALF_OPEN_TIMEOUT_KEY, 
					 					Constants.DEFAULT_CB_HALF_OPEN_TIMEOUT);
			 int closeThreshold = config.getMethodParameter(methodName, Constants.CB_CLOSE_THRESHOLD_KEY, 
					 					Constants.DEFAULT_CB_CLOSE_THRESHOLD);
			 boolean forceOpen = config.getMethodParameter(methodName, Constants.CB_FORCE_OPEN_KEY, false);
			 boolean forceClose = config.getMethodParameter(methodName, Constants.CB_FORCE_CLOSE_KEY, false);
			 
			 CircuitBreakerConfig circuitbreakerConfig = new CircuitBreakerConfig(failThreshold, volumeThreshold,
					 halfOpenTimeout, closeThreshold, forceOpen, forceClose);
			 CIRCUIT_BREAKERS.putIfAbsent(key, new CircuitBreaker(key, circuitbreakerConfig));
			 breaker = CIRCUIT_BREAKERS.get(key);
		}
		return breaker;
	}
	
	public CircuitBreakerFilter() {
		RpcMetricCollector.getInstance().registerCircuitBreakerFilter(this);
	}
	
	
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		
		CircuitBreaker breaker = null;
		String key = getKey(request);
		String methodName = request.getMethodName();
		if(invoker.getConfig().hasMethodParameter(methodName, Constants.CB_FAIL_THRESHOLD_KEY))
		{
			breaker = getOrRegisterCircuitBreaker(invoker.getConfig(), request);
		}
		
		if(breaker != null)
		{
			if(breaker.allowRequest(request))
			{
				return invoker.invoke(request);
			}
			else
			{
				boolean isProviderSide = invoker.getConfig().isProvider();
				RpcMetricCollector.getInstance().markShortCircuited(request, isProviderSide);
				RpcException cbOpenException = new RpcException(RpcException.LOCAL_CIRCUITBREAKER_OPEN_EXCEPTION, "Service: " + key + " is in circuit-breaker-open stat.");
				return new RpcResult(request.getId(), request.getSerializeType(), cbOpenException);
			}
		}
		
		return invoker.invoke(request);
	}

}
