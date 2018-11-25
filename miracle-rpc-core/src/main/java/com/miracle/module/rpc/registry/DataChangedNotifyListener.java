package com.miracle.module.rpc.registry;

import com.miracle.module.rpc.common.RpcConfig;

public interface DataChangedNotifyListener {
	RpcConfig getOriginalData();
	void notify(final RpcConfig newConfig);
}
