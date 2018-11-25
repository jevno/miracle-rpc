package com.miracle.module.rpc.core.api.wrapper.filter;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;

@Activate(group = {Constants.CONSUMER_SIDE, Constants.PROVIDER_SIDE}, 
	providerorder = Integer.MIN_VALUE+100, consumerorder = Integer.MAX_VALUE-200000)
public class DegradeFilter implements Filter {
	
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		String degradeKey = invoker.getConfig().getMethodParameter(request.getMethodName(), Constants.DEGRADE_FILTER_KEY);
		if (!StringUtils.isEmpty(degradeKey) && degradeKey.equalsIgnoreCase("true")) {
			boolean isProviderSide = invoker.getConfig().isProvider();
			RpcMetricCollector.getInstance().markDegraded(request, isProviderSide);
			int code = RpcException.DEGRADE_EXCEPTION;
			if(!isProviderSide)
			{
				code = RpcException.LOCAL_DEGRADE_EXCEPTION;
			}
			
            throw new RpcException(code,
                    new StringBuilder(64)
                            .append(invoker.getInterface().getName())
                            .append(".")
                            .append(request.getMethodName())
                            .append(" has been degraded.")
                            .toString());
		}

		return invoker.invoke(request);
	}

}
