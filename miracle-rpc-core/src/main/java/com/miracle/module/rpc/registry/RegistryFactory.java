package com.miracle.module.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.RpcConfig;

public class RegistryFactory {
	private static Logger log = Logger.getLogger(RegistryFactory.class);
	
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Map<String, RegistryService> REGISTRIES = new ConcurrentHashMap<String, RegistryService>();
	
	private static class RegistryFactoryHolder
	{
		public static final RegistryFactory FACTORY = new RegistryFactory();
	}
	
	public static RegistryFactory getInstance()
	{
		return RegistryFactoryHolder.FACTORY;
	}
	
	public static void destroyAll()
	{
		log.info("Close all registries: " + REGISTRIES.keySet());
		
		LOCK.lock();
		try
		{
			for(RegistryService registry: REGISTRIES.values())
			{
				try
				{
					registry.destroy();
				}
				catch(Throwable t)
				{
					log.error(t.getMessage(), t);
				}
			}
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	public RegistryService getRegistry(RpcConfig config)
	{
		String serviceListKey = config.getRegistryServices();
		LOCK.lock();
		try
		{
			RegistryService registry = REGISTRIES.get(serviceListKey);
			if(registry != null) {
				return registry;
			}
			registry = new ZooKeeperRegistry(config);
	
			REGISTRIES.put(serviceListKey, registry);
			return registry;
		}
		finally
		{
			LOCK.unlock();
		}
	}
}
