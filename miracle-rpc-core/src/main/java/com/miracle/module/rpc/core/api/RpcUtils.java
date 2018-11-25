package com.miracle.module.rpc.core.api;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;

public class RpcUtils {

    public static boolean isAsync(RpcConfig config, RpcRequest request) {
    	boolean isAsync ;
    	if(RpcContext.getContext().isAsyncMode())
    	{
    		isAsync = true;
    	}
    	else if (Boolean.TRUE.toString().equals(request.getAttachment(Constants.ASYNC_KEY))) {
    		isAsync = true;
    	} else {
	    	isAsync = config.getMethodParameter(request.getMethodName(), Constants.ASYNC_KEY, false);
    	}
    	return isAsync;
    }
}
