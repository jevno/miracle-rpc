package com.miracle.module.rpc.loadbalance;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
	public <T> Invoker<T> select(List<Invoker<T>> invokers, RpcConfig config, RpcRequest request) throws RpcException 
	{
        if (invokers == null || invokers.size() == 0)
            return null;
        if (invokers.size() == 1)
            return invokers.get(0);
        return doSelect(invokers, config, request);
    }

	protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, RpcConfig config, RpcRequest request);
}
