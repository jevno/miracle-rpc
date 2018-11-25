package com.miracle.module.rpc.directory;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

import java.util.List;

public interface Directory<T> {

	List<Invoker<T>> list(RpcRequest request) throws RpcException;
	
	Class<T> getInterface();
	
	RpcConfig getConfig();
	
	boolean isAvailable();
	
	void destroy();
}
