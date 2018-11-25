package com.miracle.module.rpc.cluster;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

public interface ClusterRetryStrategy {
	 <T> boolean isRetriable(Invoker<T> invoker, RpcRequest request, RpcException lastException);
}
