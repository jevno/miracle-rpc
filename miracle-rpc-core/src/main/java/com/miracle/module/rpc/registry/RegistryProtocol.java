package com.miracle.module.rpc.registry;

import com.miracle.module.rpc.cluster.Cluster;
import com.miracle.module.rpc.cluster.ClusterFactory;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;
import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;
import com.miracle.module.rpc.directory.RegistryDirectory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RegistryProtocol implements Protocol {
	private final static Logger log = Logger.getLogger(RegistryProtocol.class);
	private Protocol protocol;
	
	//interfaceName --> exporter
	private final Map<String, Exporter<?>> boundedExportMap = new ConcurrentHashMap<String, Exporter<?>>();
	
	private final ScheduledThreadPoolExecutor switchWorker = 
			new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("server-switchWorker", true));
	
	private final static long SWITCH_PAUSE_TIME_IN_SECOND = 5l;
	
	
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}
	
	public Protocol getProtocol()
	{
		return this.protocol;
	}
	
	private static RegistryProtocol INSTANCE = new RegistryProtocol();
	public static RegistryProtocol getInstance()
	{
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		// TODO Auto-generated method stub
		String key = invoker.getConfig().getServiceKey();
		Exporter<T> exporter = (Exporter<T>) boundedExportMap.get(key);
		if(exporter == null)
		{
			synchronized(boundedExportMap) //double lock check
			{
				exporter = (Exporter<T>) boundedExportMap.get(key);
				if(exporter == null)
				{
					final Exporter<T> newExporter = this.protocol.export(invoker);
					final RegistryService registry = RegistryFactory.getInstance().getRegistry(invoker.getConfig());
					registry.register(invoker.getConfig());
					registry.updateStatus(invoker.getConfig(), ServerStatus.READY);
					
					final ExportedServiceConfigUpdater<T> updater = new ExportedServiceConfigUpdater<T>(invoker);
					registry.watchRegisteredData(invoker.getConfig(), updater);
					
					exporter = new Exporter<T>(){
						
						@Override
						public void unexport() {
							try{
								registry.unwatchRegisteredData(getInvoker().getConfig(), updater);
							}
							catch(Throwable t0)
							{
								log.error(t0.getMessage(), t0);
							}
							
							try{
								registry.unregister(getInvoker().getConfig());
							}
							catch(Throwable t)
							{
								log.error(t.getMessage(), t);
							}
							
							boundedExportMap.remove(getInvoker().getConfig().getServiceKey());
							newExporter.unexport();
						}

						@Override
						public Invoker<T> getInvoker() {
							return newExporter.getInvoker();
						}
						
					};
					boundedExportMap.put(key, exporter);
				}
			}
		}
		
		return exporter;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, RpcConfig config)
			throws RpcException {
		
		final Cluster cluster = ClusterFactory.getInstance().getCluster(config);
		final RegistryService registry = RegistryFactory.getInstance().getRegistry(config);
		
		return doRefer(cluster, registry, type, config);
	}
	
	private <T> Invoker<T> doRefer(Cluster cluster, RegistryService registry, Class<T> type, RpcConfig config)
	{
		RegistryDirectory<T> directory = new RegistryDirectory<T>(type, config);
		directory.setRegistry(registry);
		directory.setRpcProtocol(protocol);
		
		registry.register(config); //register consumer configure
		
		//Need to subscribe provider path in zk, so we change side key
		directory.subscribe(config);
	
		return cluster.join(directory);
	}

	@Override
	public void destroy() {
		this.protocol.destroy();
		
		List<Exporter<?>> exporters = new ArrayList<Exporter<?>>(boundedExportMap.values());
        for(Exporter<?> exporter : exporters){
            exporter.unexport();
        }
        boundedExportMap.clear();
	}
	
	private List<Invoker<?>> getAllExportedInvokers()
	{
		List<Invoker<?>> invokerList = new ArrayList<Invoker<?>>(this.boundedExportMap.size());
		List<Exporter<?>> exporters = new ArrayList<Exporter<?>>(boundedExportMap.values());
        for(Exporter<?> exporter : exporters){
        	invokerList.add(exporter.getInvoker());
        }
        return invokerList;
	}
	
	private void pauseAll() {
		log.info("All exported service: change status to STOPPING.");
		List<Invoker<?>> invokerList = getAllExportedInvokers();
		
		if(invokerList != null && invokerList.size() > 0)
		{
			for(Invoker<?> inv : invokerList)
			{
				final RegistryService registry = RegistryFactory.getInstance().getRegistry(inv.getConfig());
				registry.updateStatus(inv.getConfig(), ServerStatus.STOPPING);
			}
		}
	}
	
	private void stopAll() {
		log.info("All exported service: change status to STOPPED.");
		List<Invoker<?>> invokerList = getAllExportedInvokers();
		
		if(invokerList != null && invokerList.size() > 0)
		{
			for(Invoker<?> inv : invokerList)
			{
				final RegistryService registry = RegistryFactory.getInstance().getRegistry(inv.getConfig());
				registry.updateStatus(inv.getConfig(), ServerStatus.STOPPED);
			}
		}
	}
	
	private void reexportAll(List<Invoker<?>> invokerList)
	{
		log.info("re-export all service.");
		if(invokerList != null && invokerList.size() > 0)
		{
			for(Invoker<?> inv : invokerList)
			{
				this.export(inv);
			}
		}
	}

	/*目前常用的都是一个服务的所有RPC接口共享同一个port及通讯层NettyServer，所以某个接口配置变化切换服务时，所有接口都会收到影响，都需要重新export；
	 * 非共享的情况暂不特别优化*/
	class SwitchServiceTask<T>{
		private final Invoker<T> originalInvoker;
		private RpcConfig newConfig;
		
		public SwitchServiceTask(Invoker<T> invoker)
		{
			this.originalInvoker = invoker;
		}
		
		private void setConfig(RpcConfig config)
		{
			this.newConfig = config;
		}
		
		public void switchService()
		{
			List<Invoker<?>> invokerList = RegistryProtocol.this.getAllExportedInvokers();
			RegistryProtocol.this.stopAll();
			RegistryProtocol.this.destroy();
			RpcMetricCollector.getInstance().reset();
			
			if(invokerList != null && invokerList.size() > 0)
			{
				Iterator<Invoker<?>> it = invokerList.iterator();
				while(it.hasNext())
				{
					if(it.next().getConfig().getServiceKey().equals(newConfig.getServiceKey()))
					{
						it.remove();
					}
				}
			}
			
			Invoker<T> newInvoker = new Invoker<T>(){

				@Override
				public RpcConfig getConfig() {
					return newConfig;
				}

				@Override
				public boolean isAvailable() {
					return originalInvoker.isAvailable();
				}

				@Override
				public void destroy() {
					originalInvoker.destroy();
				}

				@Override
				public Class<T> getInterface() {
					return originalInvoker.getInterface();
				}

				@Override
				public RpcResult invoke(RpcRequest request)
						throws RpcException {
					return originalInvoker.invoke(request);
				}
				
			};
			
			invokerList.add(newInvoker);
			RegistryProtocol.this.reexportAll(invokerList);
		}
	}
	
	class ExportedServiceConfigUpdater<T> implements DataChangedNotifyListener
	{
		private final Invoker<T> originalInvoker;
		private RpcConfig oldConfig;
		
		private SwitchServiceTask<T> switchTask = null;
		private final Lock lock = new ReentrantLock();
		
		private final Runnable switchJob = new Runnable(){

			@Override
			public void run() {
				try{
					lock.tryLock(100, TimeUnit.MILLISECONDS);
					if(switchTask != null)
					{
						switchTask.switchService();
						switchTask = null;
					}
				}
				catch(InterruptedException e)
				{
					//do nothing
				}
				finally 
				{
					lock.unlock();
				}	
			}
			
		};
		
		public ExportedServiceConfigUpdater(Invoker<T> invoker)
		{
			originalInvoker = invoker;
			oldConfig = invoker.getConfig();
		}

		@Override
		public RpcConfig getOriginalData() {
			return oldConfig;
		}

		private boolean isParamDiffAndCare(String key, String oldVal, String newVal)
		{
			boolean isSame = false;
			
			if(oldVal != null && newVal != null)
			{
				isSame = oldVal.equals(newVal);
			}
			else if(oldVal == null && newVal == null)
			{
				isSame = true;
			}
			
			if(!isSame)
			{
					if(Constants.FILTER_CONFIG_KEYS.length > 0)
					{
						for(String filterKey : Constants.FILTER_CONFIG_KEYS)
						{
							if(key.endsWith(filterKey))
							{
								return true;
							}
						}
					}
			}
			
			return false;
		}
		
		private boolean isNeedReconstructExportedService(RpcConfig oldConfig, RpcConfig newConfig)
		{
			Map<String, String> oldParams = oldConfig.getParamsMapExcludeDefault();
			Map<String, String> newParams = newConfig.getParamsMapExcludeDefault();
			
			for(String tmpKey : oldParams.keySet())
			{
				String oldVal = oldParams.get(tmpKey);
				String newVal = newParams.get(tmpKey);
				if(isParamDiffAndCare(tmpKey, oldVal, newVal)) {
					return true;
				}
			}
			
			for(String tmpKey : newParams.keySet())
			{
				String oldVal = oldParams.get(tmpKey);
				String newVal = newParams.get(tmpKey);
				if(isParamDiffAndCare(tmpKey, oldVal, newVal)) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public void notify(final RpcConfig newConfig) {
			if((ServerStatus.READY == ServerStatus.getServerStatusByRpcConfig(newConfig) || 
					ServerStatus.STOPPING == ServerStatus.getServerStatusByRpcConfig(newConfig))
					&& isNeedReconstructExportedService(oldConfig, newConfig))
			{
				log.info("Exported service: " + this.oldConfig.getServiceKey() + " is going to reconstruct on " + newConfig);
				if(RegistryProtocol.this.getProtocol() != null)
				{
					RegistryProtocol.this.pauseAll();
					
					try{
						if(lock.tryLock(1000, TimeUnit.MILLISECONDS))
						{
							if(switchTask == null)
							{
								switchTask = new SwitchServiceTask<T>(this.originalInvoker);
								switchWorker.schedule(switchJob, SWITCH_PAUSE_TIME_IN_SECOND, TimeUnit.SECONDS);
							}
							switchTask.setConfig(newConfig);
						}
					}
					catch(InterruptedException e)
					{
						//do nothing
					}
					finally 
					{
						lock.unlock();
					}
				}
			}
			this.oldConfig = newConfig;
		}
		
	}
}
