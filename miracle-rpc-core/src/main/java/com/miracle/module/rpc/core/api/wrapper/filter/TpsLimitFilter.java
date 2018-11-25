package com.miracle.module.rpc.core.api.wrapper.filter;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;
import com.miracle.module.rpc.core.api.wrapper.filter.tps.TpsLimiter;

@Activate(group = Constants.PROVIDER_SIDE, providerorder = Integer.MIN_VALUE+1000)
public class TpsLimitFilter implements Filter {

	private final TpsLimiter tpsLimiter = new TpsLimiter();
	
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		if (!tpsLimiter.isAllowable(invoker.getConfig(), request)) {
			boolean isProviderSide = invoker.getConfig().isProvider();
			RpcMetricCollector.getInstance().markTpsLimited(request, isProviderSide);
            throw new RpcException(RpcException.TPSLIMIT_EXCEPTION,
                    new StringBuilder(64)
                            .append("Failed to invoke service ")
                            .append(invoker.getInterface().getName())
                            .append(".")
                            .append(request.getMethodName())
                            .append(" because exceed max service tps.")
                            .toString());
		}

		return invoker.invoke(request);
	}

}
