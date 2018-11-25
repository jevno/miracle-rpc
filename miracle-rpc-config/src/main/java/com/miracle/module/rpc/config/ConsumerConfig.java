package com.miracle.module.rpc.config;

public class ConsumerConfig extends AbstractReferenceConfig{
	
	public void fixDefaultReferenceConfig()
	{
		appendProperties(this);
		this.fixApplicationConfig();
		this.fixRegistryConfig();
	}
}
