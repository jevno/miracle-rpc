package com.miracle.module.rpc.config;

public class ProviderConfig extends AbstractServiceConfig{

	public void fixDefaultServiceConfig()
	{
		appendProperties(this);
		this.fixApplicationConfig();
		this.fixRegistryConfig();
	}
}
