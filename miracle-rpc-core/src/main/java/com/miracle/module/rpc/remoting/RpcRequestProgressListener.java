package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;

public interface RpcRequestProgressListener {
	void requestSubmitted(RpcRequest request);
	
	void requestFinished(RpcRequest request, RpcResult response, long duration);
}
