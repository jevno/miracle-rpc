package com.miracle.module.rpc.core.api.wrapper.filter;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;

import com.miracle.module.rpc.core.api.*;

@Activate(group = {Constants.PROVIDER_SIDE}, auto = "true", providerorder = Integer.MIN_VALUE+10)
public class EchoFilter implements Filter {

	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		if(request.getMethodName().equals(Constants.ECHO_METHOD)
				&& (request.getArguments()==null || request.getArguments().length==0)) {
			return new RpcResult(request.getId(), request.getSerializeType(), 0);
		}
		return invoker.invoke(request);
	}

}
