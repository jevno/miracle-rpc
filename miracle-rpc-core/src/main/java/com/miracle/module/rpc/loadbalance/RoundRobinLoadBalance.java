package com.miracle.module.rpc.loadbalance;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.AtomicPositiveInteger;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcRequest;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RoundRobinLoadBalance extends AbstractLoadBalance {

	private final ConcurrentHashMap<String, AtomicPositiveInteger> sequences = 
			new ConcurrentHashMap<String, AtomicPositiveInteger>();
	
	@Override
	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers,
			RpcConfig config, RpcRequest request) {
		String key = request.getInterfaceName();
		
		int length = invokers.size();
		AtomicPositiveInteger sequence = sequences.get(key);
        if (sequence == null) {
            sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = sequences.get(key);
        }
        // 取模轮循
        return invokers.get(sequence.getAndIncrement() % length);
	}

}
