package com.miracle.module.rpc.loadbalance;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

import java.util.List;

public interface LoadBalance {
	
	<T> Invoker<T> select(List<Invoker<T>> invokers, RpcConfig config, RpcRequest request) throws RpcException;
}
