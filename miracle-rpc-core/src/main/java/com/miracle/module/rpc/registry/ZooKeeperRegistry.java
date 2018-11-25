package com.miracle.module.rpc.registry;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.ConcurrentHashSet;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;
import com.miracle.module.rpc.core.api.ServerStatus;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.*;
import java.util.concurrent.*;

public class ZooKeeperRegistry implements RegistryService{

	private final static Logger log = Logger.getLogger(ZooKeeperRegistry.class);
	
	private final String zkAddress;
	private final int sessConnTimeout;
	private final int sessionTimeout;
	private final CuratorZkClient zkClient;
	
	private final Set<RpcConfig> registered = new ConcurrentHashSet<RpcConfig>();
	private final Set<RpcConfig> failedRegistered = new ConcurrentHashSet<RpcConfig>();
	private final Set<RpcConfig> failedUnregistered = new ConcurrentHashSet<RpcConfig>();
	
	private final ConcurrentHashMap<String, Set<DataChangedNotifyListener>> registeredDataChanged =
			new ConcurrentHashMap<String, Set<DataChangedNotifyListener>>();
	private final ConcurrentHashMap<String, Set<DataChangedNotifyListener>> failedRegisteredDataChanged =
			new ConcurrentHashMap<String, Set<DataChangedNotifyListener>>();
	//Service data who registered ---> watcher
	private final ConcurrentHashMap<String, CuratorWatcher> registeredDataWatcherMap =
			new ConcurrentHashMap<String, CuratorWatcher>();
	
	private final ConcurrentHashMap<RpcConfig, Set<NotifyListener>> subscribed = 
			new ConcurrentHashMap<RpcConfig, Set<NotifyListener>>();
	private final ConcurrentHashMap<RpcConfig, Set<NotifyListener>> failedSubscribed =
			new ConcurrentHashMap<RpcConfig, Set<NotifyListener>>();
	//Unsubscribed operation should not fail, so no need to retry
	//service_provider who subscribed ---> watcher
	private final ConcurrentHashMap<String, CuratorWatcher> watcherMap =
			new ConcurrentHashMap<String, CuratorWatcher>();
	
	private final ScheduledExecutorService retryExecutor = 
			Executors.newScheduledThreadPool(1, new NamedThreadFactory("RegistryFailedRetryTimer", true));
	private final ScheduledFuture<?> retryFuture;
	

	
	//fix: need to add strategy for failed subscribe or register to retry
	
	public ZooKeeperRegistry(RpcConfig config)
	{
		if(config == null) {
			throw new IllegalArgumentException("registry config == null");
		}
		
		this.zkAddress = config.getParameter(Constants.ADDRESS_KEY);
		this.sessConnTimeout = config.getParameter(Constants.SESSCONN_TIMEOUT_KEY, Constants.DEFAULT_SESSCONN_TIMEOUT);
		this.sessionTimeout = config.getParameter(Constants.SESSION_TIMEOUT_KEY, Constants.DEFAULT_SESSION_TIMEOUT);
		
		zkClient = new CuratorZkClient(this.zkAddress, this.sessConnTimeout, this.sessionTimeout);
		zkClient.addConnLostListener(new CuratorZkClient.ZkConnLostListener() {
			
			@Override
			public void sessionExpired() {
				recover();
			}
		});
		
		long retryPeriod = config.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
		retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				retry();
			}
			
		}, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
	}
	
	private String getServicePath(final RpcConfig config)
	{
		String service = config.getServiceKey();
		if(config.isProvider())
		{
			return Constants.ROOT_PATH + service + Constants.PATH_SEPARATOR + Constants.PROVIDERS_PATH;
		}
		else
		{
			return Constants.ROOT_PATH + service + Constants.PATH_SEPARATOR + Constants.CONSUMERS_PATH;
		}
	}
	
	private String getFullServicePath(final RpcConfig config)
	{
		String path = getServicePath(config);
		if(config.isProvider()) {
			String serverAddr = config.getAddress();
			return path + Constants.PATH_SEPARATOR + serverAddr;
		}
		else
		{
			String appTag = config.getAppTag();
			return path + Constants.PATH_SEPARATOR + appTag;
		}
	}
	
	@Override
	public void register(RpcConfig config) {
		// TODO Auto-generated method stub
		boolean bSucc = false;
		if(config == null)
			throw new IllegalArgumentException("registry config == null");
		bSucc = doRegister(config);
		if(bSucc)
		{
			this.registered.add(config);
			log.info("register: " + config);
		}
		else
		{
			log.error("register failed: " + config);
		}
	}
	
	protected boolean doRegister(RpcConfig config)
	{
		this.failedRegistered.remove(config);
		String servicePath = getFullServicePath(config);
		try{//check exist or not, if exist then delete first
			if(zkClient.checkExist(servicePath))
			{
				zkClient.delete(servicePath);
			}
			String serviceConfigString = config.toJsonString();
			zkClient.createEphemeral(servicePath, serviceConfigString);
			
			return true;
		}
		catch(Exception e)
		{
			log.error("Register service [" + servicePath + "] failed: " + e.getMessage(), e);
			this.failedRegistered.add(config);
		}
		return false;
	}
	
	private void addFailedWatch(String path, DataChangedNotifyListener listener) {
        Set<DataChangedNotifyListener> listeners = failedRegisteredDataChanged.get(path);
        if (listeners == null) {
        	failedRegisteredDataChanged.putIfAbsent(path, new ConcurrentHashSet<DataChangedNotifyListener>());
            listeners = failedRegisteredDataChanged.get(path);
        }
        listeners.add(listener);
    }

    private void removeFailedWatch(String path, DataChangedNotifyListener listener) {
        Set<DataChangedNotifyListener> listeners = failedRegisteredDataChanged.get(path);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
	@Override
	public void watchRegisteredData(RpcConfig config, DataChangedNotifyListener listener) {
		if(config == null) {
			throw new IllegalArgumentException("watch registered service config == null");
		}
		if(listener == null) {
			throw new IllegalArgumentException("watch registered service listener == null");
		}
		
		String fullPath = this.getFullServicePath(config);
		doWatch(fullPath, listener);
	}
	
	private boolean doWatch(String path, DataChangedNotifyListener listener) {
		CuratorWatcher watcher = this.registeredDataWatcherMap.get(path);
		if(watcher == null)
		{
			synchronized(this)
			{
				watcher = this.registeredDataWatcherMap.get(path);
				if(watcher == null)
				{
					watcher = new RegisteredDataChangedWatcher(path);
					watcherMap.put(path, watcher);
				}
			}
		}
		((RegisteredDataChangedWatcher)watcher).addListener(listener);
		
		this.removeFailedWatch(path, listener);
		RpcConfig origConfig = null;
		try
		{
			origConfig = this.getServiceDataAndWatch(path, watcher);
			
			Set<DataChangedNotifyListener> listeners = this.registeredDataChanged.get(path);
			if(listeners == null)
			{
				this.registeredDataChanged.putIfAbsent(path, new ConcurrentHashSet<DataChangedNotifyListener>());
				listeners = this.registeredDataChanged.get(path);
			}
			listeners.add(listener);
			log.info("watch service successfully: " + path);
		}
		catch(Exception e)
		{
			this.addFailedWatch(path, listener);
			log.error("Watch service: " + path + " failed" , e);
			return false;
		}
		
		listener.notify(origConfig);
		
		return true;
	}
	
	@Override
	public void unwatchRegisteredData(RpcConfig config, DataChangedNotifyListener listener) {
		if(config == null) {
			throw new IllegalArgumentException("unwatch provider service config == null");
		}
		if(listener == null) {
			throw new IllegalArgumentException("unwatch provider service listener == null");
		}
		
		String providerFullPath = this.getFullServicePath(config);
		doUnwatch(providerFullPath, listener);
	}
	
	protected boolean doUnwatch(String path, DataChangedNotifyListener listener)
	{
		CuratorWatcher watcher = this.watcherMap.get(path);
		if(watcher != null)
		{
			synchronized(this)
			{
				RegisteredDataChangedWatcher myWatcher = (RegisteredDataChangedWatcher)watcher;
				myWatcher.removeListener(listener);
				if(myWatcher.getListenerSize() == 0)
				{
					this.watcherMap.remove(path);
				}
			}
		}
		Set<DataChangedNotifyListener> listeners = this.registeredDataChanged.get(path);
		if(listeners != null)
		{
			listeners.remove(listener);
		}
		log.info("unwatch service successfully: " + path);
		
		return true;
	}
	
	@Override
	public void unregister(RpcConfig config) {
		// TODO Auto-generated method stub
		boolean bSucc = false;
		if(config == null) {
			throw new IllegalArgumentException("unregister config == null");
		}
		bSucc = doUnregister(config);
		if(bSucc)
		{
			this.registered.remove(config);
			log.info("Unregister: " + config);
		}
		else
		{
			log.error("Unregister: " + config);
		}
	}
	
	protected boolean doUnregister(RpcConfig config)
	{
		this.failedUnregistered.remove(config);
		String servicePath = getFullServicePath(config);
		try{
			zkClient.delete(servicePath);
			return true;
		}
		catch(Exception e)
		{
			this.failedUnregistered.add(config);
			log.error("Unregister service [" + servicePath + "] failed: " + e.getMessage(), e);
		}
		return false;
	}
	
	@Override
	public void updateStatus(RpcConfig config, ServerStatus status) {
		String servicePath = getFullServicePath(config);
		
		try {
			if(zkClient.checkExist(servicePath))
			{
				RpcConfig origConfig = this.getServiceDataAndWatch(servicePath, null);
				RpcConfig newConfig = origConfig.addParameter(status.getKey(), status.getStatus());
				zkClient.setData(servicePath, newConfig.toJsonString());
			}
		} catch (Exception e) {
			log.error("Update server status failed " + config, e);
		}
	}

	
	private void addFailedSubscribed(RpcConfig config, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(config);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(config, new ConcurrentHashSet<NotifyListener>());
            listeners = failedSubscribed.get(config);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(RpcConfig config, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(config);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

	@Override
	public void subscribe(RpcConfig config, NotifyListener listener) {
		// TODO Auto-generated method stub
		boolean bSucc = false;
		if(config == null) {
			throw new IllegalArgumentException("subscribe config == null");
		}
		if(listener == null) {
			throw new IllegalArgumentException("subscribe listener == null");
		}
		
		bSucc = doSubscribe(config, listener);
		if(bSucc)
		{
			Set<NotifyListener> listeners = this.subscribed.get(config);
			if(listeners == null)
			{
				this.subscribed.putIfAbsent(config, new ConcurrentHashSet<NotifyListener>());
				listeners = this.subscribed.get(config);
			}
			listeners.add(listener);
			log.info("subscribe: " + config);
		}
		else
		{
			log.error("subscribe: " + config);
		}
	}
	
	private List<RpcConfig> getServiceListAndWatch(final String providerPath, final CuratorWatcher watcher) throws Exception
	{
		List<RpcConfig> srvList = new ArrayList<RpcConfig>();

		List<String> children = zkClient.getChildren(providerPath, watcher);
		
		if(children != null && children.size() > 0)
		{
			for(String child : children)
			{
				String childPath = providerPath + Constants.PATH_SEPARATOR + child;
				String childData = zkClient.getData(childPath, watcher);
				if(childData != null)
				{
					RpcConfig childConfig = RpcConfig.parseFromJsonString(childData);
					if(childConfig != null)
					{
						srvList.add(childConfig);
					}
				}
			}
		}
		return srvList;
	}
	
	protected boolean doSubscribe(RpcConfig config, NotifyListener listener)
	{
		String providerPath = this.getServicePath(config);
		CuratorWatcher watcher = this.watcherMap.get(providerPath);
		if(watcher == null)
		{
			synchronized(this)
			{
				watcher = this.watcherMap.get(providerPath);
				if(watcher == null)
				{
					watcher = new ServiceChangedWatcher(config);
					watcherMap.put(providerPath, watcher);
				}
			}
		}
		((ServiceChangedWatcher)watcher).addListener(listener);
		
		this.removeFailedSubscribed(config, listener);
		List<RpcConfig> srvList = null;
		try
		{
			srvList = getServiceListAndWatch(providerPath, watcher);
		}
		catch(Exception e)
		{
			this.addFailedSubscribed(config, listener);
			log.error("Subscribe service: " + providerPath + " failed" , e);
			return false;
		}
		
		listener.notify(srvList);
		
		return true;
	}

	
	private RpcConfig getServiceDataAndWatch(final String path, final CuratorWatcher watcher) throws Exception
	{
		RpcConfig config = null;
		String data = zkClient.getData(path, watcher);
		if(data != null)
		{
			config = RpcConfig.parseFromJsonString(data);
		}
		return config;
	}
	
	@Override
	public void unsubscribe(RpcConfig config, NotifyListener listener) {
		// TODO Auto-generated method stub
		boolean bSucc = false;
		if(config == null) {
			throw new IllegalArgumentException("unsubscribe config == null");
		}
		if(listener == null) {
			throw new IllegalArgumentException("unsubscribe listener == null");
		}
		
		bSucc = doUnsubscribe(config, listener);
		if(bSucc)
		{
			Set<NotifyListener> listeners = this.subscribed.get(config);
			if(listeners != null)
			{
				listeners.remove(listener);
			}
			log.info("unsubscribe: " + config);
		}
		else
		{
			log.error("unsubscribe: " + config);
		}
	}
	
	protected boolean doUnsubscribe(RpcConfig config, NotifyListener listener)
	{
		String providerPath = this.getServicePath(config);
		CuratorWatcher watcher = this.watcherMap.get(providerPath);
		if(watcher != null)
		{
			synchronized(this)
			{
				ServiceChangedWatcher myWatcher = (ServiceChangedWatcher)watcher;
				myWatcher.removeListener(listener);
				if(myWatcher.getListenerSize() == 0)
				{
					this.watcherMap.remove(providerPath);
				}
			}
		}
		return true;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return zkClient.isAvailable();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (log.isInfoEnabled()){
			log.info("Destroy registry:" + this.zkAddress);
        }
		if(retryFuture != null)
		{
			try{
				retryFuture.cancel(true);
			}
			catch(Throwable t)
			{
				log.warn("Unexpected exception occured when cancel registry retryTimer.", t);
			}
		}
        Set<RpcConfig> destroyRegistered = new HashSet<RpcConfig>(this.registered);
        if (! destroyRegistered.isEmpty()) {
            for (RpcConfig config : destroyRegistered) {
                try {
                    unregister(config);
                    if (log.isInfoEnabled()) {
                    	log.info("Destroy unregister config " + config);
                    }
                } catch (Throwable t) {
                    log.warn("Failed to unregister config " + config + " on destroy, cause: " + t.getMessage(), t);
                }
            }
        }
        
        Map<String, Set<DataChangedNotifyListener>> destroyWatched = 
        		new HashMap<>(this.registeredDataChanged);
        if(!destroyWatched.isEmpty())
        {
        	for(Map.Entry<String, Set<DataChangedNotifyListener>> entry: destroyWatched.entrySet())
        	{
        		String tmpServicePath = entry.getKey();
        		if(entry.getValue() != null && !entry.getValue().isEmpty())
        		{
        			for(DataChangedNotifyListener listener : entry.getValue())
        			{
        				this.doUnwatch(tmpServicePath, listener);
        			}
        		}
        	}
        }
        
        Map<RpcConfig, Set<NotifyListener>> destroySubscribed = 
        		new HashMap<RpcConfig, Set<NotifyListener>>(this.subscribed);
        if (! destroySubscribed.isEmpty()) {
            for (Map.Entry<RpcConfig, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
            	RpcConfig config = entry.getKey();
            	if(entry.getValue() != null && !entry.getValue().isEmpty())
            	{
	                for (NotifyListener listener : entry.getValue()) {
	                    try {
	                        unsubscribe(config, listener);
	                        if (log.isInfoEnabled()) {
	                            log.info("Destroy unsubscribe url " + config);
	                        }
	                    } catch (Throwable t) {
	                        log.warn("Failed to unsubscribe url " + config + " on destroy, cause: " +t.getMessage(), t);
	                    }
	                }
            	}
            }
        }
	}
	
	public void recover()
	{
		log.info("Recover registry: " + zkAddress);
		
		Set<RpcConfig> recoverRegistered = new HashSet<RpcConfig>(this.registered);
		if(!recoverRegistered.isEmpty())
		{
			for(RpcConfig config : recoverRegistered)
			{
				doRegister(config);
			}
		}
		
		Map<String, Set<DataChangedNotifyListener>> recoverWatched = 
        		new HashMap<>(this.registeredDataChanged);
        if(!recoverWatched.isEmpty())
        {
        	for(String tmpSrvPath : recoverWatched.keySet())
        	{
        		Set<DataChangedNotifyListener> listeners = recoverWatched.get(tmpSrvPath);
        		if(listeners != null && listeners.size() > 0)
        		{
        			for(DataChangedNotifyListener listener : listeners)
        			{
        				doWatch(tmpSrvPath, listener);
        			}
        		}
        	}
        }
		
	    Map<RpcConfig, Set<NotifyListener>> recoverSubscribed = 
        		new HashMap<RpcConfig, Set<NotifyListener>>(this.subscribed);
        if (! recoverSubscribed.isEmpty()) {
        	for(RpcConfig config : recoverSubscribed.keySet())
        	{
        		Set<NotifyListener> listeners = recoverSubscribed.get(config);
        		if(listeners != null && listeners.size() > 0)
        		{
        			for(NotifyListener listener : listeners)
        			{
        				doSubscribe(config, listener);
        			}
        		}
        	}
        }
	}
	
	protected void retry()
	{
		if(!this.failedRegistered.isEmpty())
		{
			Set<RpcConfig> failed = new HashSet<RpcConfig>(failedRegistered);
			if(failed.size() > 0)
			{
				log.info("Retry register " + failed);
				for(RpcConfig config : failed)
				{
					try{
						this.register(config);
					}
					catch(Throwable t)
					{
						log.error("Unexpected exception occured when retry register " + config, t);
					}
				}
			}
		}
		if(!this.failedUnregistered.isEmpty())
		{
			Set<RpcConfig> failed = new HashSet<RpcConfig>(failedUnregistered);
			if(failed.size() > 0)
			{
				log.info("Retry unregister " + failed);
				for(RpcConfig config : failed)
				{
					try{
						this.unregister(config);
					}
					catch(Throwable t)
					{
						log.error("Unexpected exception occured when retry unregister " + config, t);
					}
				}
			}
		}
		
		if(!this.failedRegisteredDataChanged.isEmpty())
		{
			Map<String, Set<DataChangedNotifyListener>> failedWatch = 
					new HashMap<>(this.failedRegisteredDataChanged);
			for (Map.Entry<String, Set<DataChangedNotifyListener>> entry : new HashMap<String, Set<DataChangedNotifyListener>>(failedWatch).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                	failedWatch.remove(entry.getKey());
                }
            }
			if(failedWatch.size() > 0)
			{
				log.info("Retry watch " + failedWatch.keySet());
				for(Map.Entry<String, Set<DataChangedNotifyListener>> entry : failedWatch.entrySet())
				{
					String tmpSrvPath = entry.getKey();
					Set<DataChangedNotifyListener> listeners = entry.getValue();
					if(listeners != null && listeners.size() > 0)
					{
						for(DataChangedNotifyListener listener : listeners)
						{
							try{
								this.doWatch(tmpSrvPath, listener);
							}
							catch(Throwable t)
							{
								log.error("Unexpected exception occured when retry watch " + tmpSrvPath, t);
							}
						}
					}
				}
			}
		}
		
		if(!this.failedSubscribed.isEmpty())
		{
			Map<RpcConfig, Set<NotifyListener>> failed = 
					new HashMap<RpcConfig, Set<NotifyListener>>(this.failedSubscribed);
			//here need to new hashmap because we can not remove element when loop a hashmap.
			for (Map.Entry<RpcConfig, Set<NotifyListener>> entry : new HashMap<RpcConfig, Set<NotifyListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
			if(failed.size() > 0)
			{
				log.info("Retry subscribe " + failed);
				for(Map.Entry<RpcConfig, Set<NotifyListener>> entry : failed.entrySet())
				{
					RpcConfig config = entry.getKey();
					Set<NotifyListener> listeners = entry.getValue();
					if(listeners != null && listeners.size() > 0)
					{
						for(NotifyListener listener : listeners)
						{
							try{
								this.subscribe(config, listener);
							}
							catch(Throwable t)
							{
								log.error("Unexpected exception occured when retry subscribe " + config, t);
							}
						}
					}
				}
			}
		}
	}

	public class RegisteredDataChangedWatcher implements CuratorWatcher
	{
		private final String path;
		private ConcurrentHashSet<DataChangedNotifyListener> notifySet = new ConcurrentHashSet<DataChangedNotifyListener>();
		
		public RegisteredDataChangedWatcher(String path)
		{
			this.path = path;
		}
		
		public int getListenerSize()
		{
			return notifySet.size();
		}
		
		public void addListener(DataChangedNotifyListener listener)
		{
			notifySet.add(listener);
		}
		
		public void removeListener(DataChangedNotifyListener listener)
		{
			notifySet.remove(listener);
		}
		
		@Override
		public void process(WatchedEvent event) throws Exception {
			if(event.getType() == Watcher.Event.EventType.NodeDataChanged)
			{
				if(notifySet.size() > 0)
				{
					RpcConfig newConfig = ZooKeeperRegistry.this.getServiceDataAndWatch(path, this);
							
					for(DataChangedNotifyListener listener : notifySet)
					{
						listener.notify(newConfig);
					}
				}
			}
		}
		
	}
	
	public class ServiceChangedWatcher implements CuratorWatcher
	{
		private final RpcConfig config;
		private ConcurrentHashSet<NotifyListener> notifySet = new ConcurrentHashSet<NotifyListener>();
		
		public ServiceChangedWatcher(RpcConfig config)
		{
			this.config = config;
		}
		
		public int getListenerSize()
		{
			return notifySet.size();
		}
		
		public void addListener(NotifyListener listener)
		{
			notifySet.add(listener);
		}
		
		public void removeListener(NotifyListener listener)
		{
			notifySet.remove(listener);
		}
		
		@Override
		public void process(WatchedEvent event) throws Exception {
			if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged ||
					event.getType() == Watcher.Event.EventType.NodeDataChanged)
			{
				if(notifySet.size() > 0)
				{
					String providerPath = ZooKeeperRegistry.this.getServicePath(config);
					List<RpcConfig> srvList = ZooKeeperRegistry.this.getServiceListAndWatch(providerPath, this);
							
					for(NotifyListener listener : notifySet)
					{
						listener.notify(srvList);
					}
				}
			}
		}
		
	}
}
