package com.miracle.module.rpc.router;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.RpcException;

import java.util.List;

public interface Router {
	RpcConfig getConfig();
	
	List<RpcConfig> route(List<RpcConfig> providerConfigs, RpcConfig consumerConfig) throws RpcException;
}
