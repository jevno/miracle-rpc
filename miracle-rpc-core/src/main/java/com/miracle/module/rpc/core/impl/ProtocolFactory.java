package com.miracle.module.rpc.core.impl;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.registry.RegistryProtocol;
import com.miracle.module.rpc.core.api.Protocol;

public class ProtocolFactory {

	public static Protocol createProtocol(RpcConfig config)
	{
		RegistryProtocol registryProtocol = RegistryProtocol.getInstance();
		if(registryProtocol.getProtocol() == null)
		{
			registryProtocol.setProtocol(DefaultProtocol.getInstance());
		}
		return registryProtocol;
	}
	
	static 
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{

			@Override
			public void run() {
				RegistryProtocol.getInstance().destroy();
			}
			
		}, "KKRpcShutdownHook"));
	}
}
