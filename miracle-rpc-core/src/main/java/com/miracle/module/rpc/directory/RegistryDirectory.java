package com.miracle.module.rpc.directory;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.registry.DataChangedNotifyListener;
import com.miracle.module.rpc.registry.NotifyListener;
import com.miracle.module.rpc.registry.RegistryService;
import com.miracle.module.rpc.router.GroupRouter;
import com.miracle.module.rpc.router.IpRouter;
import com.miracle.module.rpc.router.Router;

import com.miracle.module.rpc.core.api.*;
import org.apache.log4j.Logger;

import java.util.*;

public class RegistryDirectory<T> implements Directory<T>, NotifyListener, DataChangedNotifyListener {

	private static final Logger log = Logger.getLogger(RegistryDirectory.class);
	private volatile boolean destroyed = false;
	private final RpcConfig config;
	private volatile RpcConfig consumerConfig;
	private final Class<T> serviceType;
	private final String serviceKey;
	private Protocol rpcProtocol;
	private RegistryService registry;
	private volatile List<Router> routers = null;
	private volatile List<RpcConfig> providerConfigList = null;
	
	private volatile Set<String> stoppingServiceKeySet;
	private volatile Set<String> tmpStoppingServiceKeySet;
	private volatile Map<String, Invoker<T>> invokerMap;
	
	private volatile boolean forbidden = false;
	
	public Protocol getRpcProtocol() {
		return rpcProtocol;
	}

	public void setRpcProtocol(Protocol rpcProtocol) {
		this.rpcProtocol = rpcProtocol;
	}

	public RegistryService getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryService registry) {
		this.registry = registry;
	}

	public RegistryDirectory(Class<T> serviceType, RpcConfig config)
	{
		if(serviceType == null) {
			throw new IllegalArgumentException("service type is null");
		}
		if(config == null) {
			throw new IllegalArgumentException("registry directory config is null");
		}
		this.serviceType = serviceType;
		this.config = config;
		this.consumerConfig = config;
		this.serviceKey = config.getServiceKey();
				
		prepareRouters(config);
	}
	
	private void prepareRouters(RpcConfig config)
	{
		String group = config.getParameter(Constants.GROUP_KEY);
		String iprouter = config.getParameter(Constants.IPROUTER_KEY);
		if(!StringUtils.isBlank(group) || !StringUtils.isBlank(iprouter))
		{
			List<Router> newRouters = new ArrayList<Router>();
			if(!StringUtils.isBlank(group))
			{
				newRouters.add(new GroupRouter(config));
			}
			if(!StringUtils.isBlank(iprouter))
			{
				newRouters.add(new IpRouter(config));
			}
			this.routers = newRouters;
		}
	}
	
	@Override
	public synchronized void notify(List<RpcConfig> srvList) {
		List<RpcConfig> invokerConfigs = srvList;
		refreshInvoker(invokerConfigs);
		this.providerConfigList = srvList;
	}
	
	private List<RpcConfig> filterWorkableProviderConfig(List<RpcConfig> providerConfigs)
	{
		if(providerConfigs != null && providerConfigs.size() > 0)
		{
			List<RpcConfig> filteredConfigs = new ArrayList<RpcConfig>(providerConfigs.size());
			for(RpcConfig config : providerConfigs)
			{
				ServerStatus status = ServerStatus.getServerStatusByRpcConfig(config);
				if(status == ServerStatus.READY || status == ServerStatus.STOPPING)
				{
					filteredConfigs.add(config);
				}
			}
			return filteredConfigs;
		}
		
		return providerConfigs;
	}
	
	private void refreshInvoker(List<RpcConfig> invokerConfigs)
	{
		invokerConfigs = filterWorkableProviderConfig(invokerConfigs);
		
		if(invokerConfigs == null || invokerConfigs.size() <= 0)
		{
			this.forbidden = true;
			this.destroyAllInvokers();
		}
		else
		{
			Map<String, Invoker<T>> oldInvokerMap = this.invokerMap;
			Map<String, Invoker<T>> newInvokerMap = toInvokers(invokerConfigs);
			this.invokerMap = newInvokerMap;
			this.stoppingServiceKeySet = this.tmpStoppingServiceKeySet;
			destroyInvalidInvokers(oldInvokerMap, newInvokerMap);
			this.forbidden = false;
		}
	}
	
	private RpcConfig mergeConfig(RpcConfig providerConfig)
	{
		//fix later: merge consumerConfig and providerConfig
		Map<String, String> params = new HashMap<String, String>();
		
		for(String key: Constants.MERGE_PROVIDER_CONFIG_KEYS)
		{
			if(providerConfig.hasParameter(key))
			{
				params.put(key, providerConfig.getParameter(key));
			}
		}
		
		RpcConfig newConfig = this.consumerConfig.addParameters(params);
		return newConfig;
	}
	
	private Map<String, Invoker<T>> toInvokers(List<RpcConfig> invokerConfigs)
	{
		List<Router> localRouters = this.routers; // local reference
        if (localRouters != null && localRouters.size() > 0) {
            for (Router router: localRouters){
            	invokerConfigs = router.route(invokerConfigs, getConsumerConfig());
            }
        }
        this.tmpStoppingServiceKeySet = new HashSet<String>();
		Map<String, Invoker<T>> newInvokerMap = new HashMap<String, Invoker<T>>();
		if(invokerConfigs == null || invokerConfigs.size() <= 0)
			return newInvokerMap;
		Set<String> configKeySet = new HashSet<String>();
		for(RpcConfig providerConfig : invokerConfigs)
		{
			RpcConfig mergedConfig = mergeConfig(providerConfig);
			String configKey = mergedConfig.toUniqueString(Constants.TO_UNIQUE_STRING_IGNORED_KEYS);
			if(configKeySet.contains(configKey))
				continue;
			configKeySet.add(configKey);
			Map<String, Invoker<T>> localInvokerMap = this.invokerMap; // local reference
	        Invoker<T> invoker = localInvokerMap == null ? null : localInvokerMap.get(configKey);
	        if (invoker == null) { // 缓存中没有，重新refer
                try {	
                	invoker = this.rpcProtocol.refer(serviceType, mergedConfig);
                } catch (Throwable t) {
                    log.error("Failed to refer invoker for interface:"+serviceType+",config:("+mergedConfig+")" + t.getMessage(), t);
                }
                if (invoker != null) { // 将新的引用放入缓存
                	newInvokerMap.put(configKey, invoker);
                }
            }else {
            	newInvokerMap.put(configKey, invoker);
            }
	        if(ServerStatus.getServerStatusByRpcConfig(providerConfig) == ServerStatus.STOPPING)
	        {
	        	 this.tmpStoppingServiceKeySet.add(configKey);
	        }
		}
		configKeySet.clear();
		return newInvokerMap;
	}
	
	
	public void subscribe(RpcConfig config)
	{
		this.registry.subscribe(config.addParameter(Constants.SIDE_KEY, Constants.PROVIDER_SIDE), this);
		this.registry.watchRegisteredData(config, this);
	}
	
	

	@Override
	public List<Invoker<T>> list(RpcRequest request) throws RpcException {
		// TODO Auto-generated method stub
		if(this.destroyed)
			throw new RpcException("Directory already destroyed config: " + config);
		
		List<Invoker<T>> invokers = doList(request);
		
		return invokers;
	}
	
	protected List<Invoker<T>> doList(RpcRequest request) throws RpcException
	{
		if(this.forbidden)
		{
			throw new RpcException(RpcException.NOSERVICE_EXCEPTION, 
					"Currently no accessible service: " + this.getInterface().getName());
		}
		List<Invoker<T>> invokers = null;
		if(this.invokerMap != null)
		{
			invokers = new ArrayList<Invoker<T>>(this.invokerMap.values());
		}
		else
		{
			invokers = new ArrayList<Invoker<T>>(); //to make sure return list not be null
		}
		
		//filter stopping provider refer invoker while doList to submit a request
		if(this.stoppingServiceKeySet != null && this.stoppingServiceKeySet.size() > 0)
		{
			List<String> keys = new ArrayList<String>(this.stoppingServiceKeySet);
			for(String key: keys)
			{
				Invoker<T> stoppingInvoker = this.invokerMap.get(key);
				invokers.remove(stoppingInvoker);
			}
		}
		
		return invokers;
	}

	@Override
	public Class<T> getInterface() {
		// TODO Auto-generated method stub
		return this.serviceType;
	}

	@Override
	public boolean isAvailable() {
		if(isDestroyed())
		{
			return false;
		}
		Map<String, Invoker<T>> localInvokerMap = invokerMap;
		if (localInvokerMap != null && localInvokerMap.size() > 0) {
		    for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localInvokerMap.values())) {
		        if (invoker.isAvailable()) {
		            return true;
		        }
		    }
		}
		return false;
	}

	private void destroyAllInvokers()
	{
		Map<String, Invoker<T>> localInvokerMap = this.invokerMap; // local reference
        if(localInvokerMap != null) {
            for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localInvokerMap.values())) {
                try {
                    invoker.destroy();
                } catch (Throwable t) {
                    log.warn("Failed to destroy service " + serviceKey + " to provider " + invoker.getConfig(), t);
                }
            }
            localInvokerMap.clear();
        }
	}
	
	private void destroyInvalidInvokers(Map<String, Invoker<T>> oldInvokerMap, Map<String, Invoker<T>> newInvokerMap)
	{
		if(newInvokerMap == null || newInvokerMap.size() == 0)
		{
			destroyAllInvokers();
		}
		List<String> invalidInvokers = null;
		if(oldInvokerMap != null)
		{
			Collection<Invoker<T>> newInvokers = newInvokerMap.values();
			for(Map.Entry<String, Invoker<T>> entry : oldInvokerMap.entrySet())
			{
				if(!newInvokers.contains(entry.getValue()))
				{
					if(invalidInvokers == null)
					{
						invalidInvokers = new ArrayList<String>();
					}
					invalidInvokers.add(entry.getKey());
				}
			}
		}
		
		if(invalidInvokers != null)
		{
			for(String invalidKey : invalidInvokers)
			{
				if(invalidKey != null)
				{
					Invoker<T> invoker = oldInvokerMap.remove(invalidKey);
					if(invoker != null)
					{
						try
						{
							invoker.destroy();
							log.info("Destroy invalid invoker: " + invoker.getConfig());
						}
						catch(Throwable t)
						{
							log.warn("Destroy invalid invoker: " + invoker.getConfig() + " failed " + t.getMessage(), t);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void destroy() {
		if(this.isDestroyed())
			return;
		try{
			if(this.consumerConfig != null && registry != null && registry.isAvailable())
			{
				registry.unsubscribe(this.consumerConfig.addParameter(Constants.SIDE_KEY, Constants.PROVIDER_SIDE), this);
			}
		}
		catch(Throwable t)
		{
			log.error("unsubscribe failed: " + this.consumerConfig, t);
		}
		this.destroyed = true;
		destroyAllInvokers();
	}
	
	public boolean isDestroyed()
	{
		return this.destroyed;
	}

	public RpcConfig getConfig()
	{
		return this.config;
	}
	
	public RpcConfig getConsumerConfig()
	{
		return this.consumerConfig;
	}

	@Override
	public RpcConfig getOriginalData() {
		return this.consumerConfig;
	}

	@Override
	public void notify(RpcConfig newConfig) {
		if(newConfig != null)
		{
			if(!this.consumerConfig.toUniqueString(null).equals(newConfig.toUniqueString(null)))
			{
				this.consumerConfig = newConfig;
				this.prepareRouters(newConfig);
				if(this.providerConfigList != null)
				{
					this.refreshInvoker(providerConfigList);
				}
			}
		}
	}
}
