package com.miracle.module.rpc.registry;

import java.util.List;

import com.miracle.module.rpc.common.RpcConfig;

public interface NotifyListener {
	void notify(List<RpcConfig> srvList);
}
