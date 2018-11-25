package com.miracle.module.rpc.registry;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.ServerStatus;

public interface RegistryService {
	boolean isAvailable();
	
	void destroy();
	
	void register(RpcConfig config);
	
	void watchRegisteredData(RpcConfig config, DataChangedNotifyListener listener);
	
	void unwatchRegisteredData(RpcConfig config, DataChangedNotifyListener listener);
	
	void unregister(RpcConfig config);
	
	void subscribe(RpcConfig config, NotifyListener listener);
	
	void unsubscribe(RpcConfig config, NotifyListener listener);
	
	void updateStatus(RpcConfig config, ServerStatus status);
}
