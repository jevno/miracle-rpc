package com.miracle.module.rpc.core.api.wrapper.filter;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;

@Activate(group = {Constants.CONSUMER_SIDE, Constants.PROVIDER_SIDE}, 
	auto = "true", providerorder = Integer.MIN_VALUE+400000, consumerorder = Integer.MAX_VALUE-10)
public class RpcStatusFilter implements Filter {
    
	private RpcMetricCollector rpcMetricCollector = RpcMetricCollector.getInstance();
	
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		
		long start = System.currentTimeMillis();
		boolean isProviderSide = invoker.getConfig().isProvider();
		if(isProviderSide)
		{
			rpcMetricCollector.updateStatusForRequestSubmitted(request, true);
		}
		RpcResult result = null;
		long elapsed = 0;
		try
		{
			result = invoker.invoke(request);
			elapsed = System.currentTimeMillis()-start;
			return result;
		}
		catch(RpcException e)
		{
			result = new RpcResult(request.getId(), request.getSerializeType(), e);
			throw e;
		}
		finally
		{
			if(isProviderSide)
			{
				rpcMetricCollector.updateStatusForRequestResponsed(request, result, elapsed, true);
			}
		}
	}

}
