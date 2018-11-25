package com.miracle.module.rpc.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import com.miracle.module.rpc.common.utils.ConcurrentHashSet;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;

public class CuratorZkClient {
	private static Logger log = Logger.getLogger(CuratorZkClient.class);
	private CuratorFramework zkClient;
	private final int maxWaitTime = 10 * 1000;
	private ConcurrentHashSet<ZkConnLostListener> stateListeners = 
			new ConcurrentHashSet<ZkConnLostListener>(); 
	
	public CuratorZkClient(String zkAddr, int connTimeout, int sessionTimeout)
	{
		zkClient = CuratorFrameworkFactory.builder().connectString(zkAddr)
				.connectionTimeoutMs(connTimeout)
				.sessionTimeoutMs(sessionTimeout)
				.retryPolicy(new RetryNTimes(5, 2000)) //适配Curator2.8.0 ConnectionStateListener LOST event
				.build();
		
		zkClient.start();
		boolean isConnected = false;
		try{
			isConnected = zkClient.blockUntilConnected(maxWaitTime, TimeUnit.MILLISECONDS);
		}
		catch(InterruptedException e)
		{
			log.warn("Woo po, Connect zk server " + zkAddr + " interrupted~");
		}
		if(isConnected)
		{
			log.info("Connected zk server " + zkAddr + " successfully!");
		}
		else
		{
			log.error("Failed to connect zk server " + zkAddr);
		}
		zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener(){

			@Override
			public void stateChanged(CuratorFramework client,
					ConnectionState newState) {
				log.info("Zk connection state changed to new state: " + newState);
				if(newState == ConnectionState.RECONNECTED)
				{
					if(CuratorZkClient.this.stateListeners.size() > 0)
					{
						List<ZkConnLostListener> listeners = 
								new ArrayList<ZkConnLostListener>(CuratorZkClient.this.stateListeners);
						for(ZkConnLostListener listener : listeners)
						{
							listener.sessionExpired();
						}
					}
				}
			}
			
		});
	}
	
	public CuratorFramework getInnerZk()
	{
		return this.zkClient;
	}
	
	public boolean isAvailable()
	{
		if(this.zkClient != null)
		{
			return this.zkClient.getZookeeperClient().isConnected();
		}
		return false;
	}
	
	public void createPersistent(String path) throws Exception
	{
		Stat stat = zkClient.checkExists().forPath(path);
		if(stat == null)
		{
			zkClient.create().creatingParentsIfNeeded()
				.withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
		}
	}
	
	public void createEphemeral(String path) throws Exception
	{
		zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
			.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
	}
	
	public void createEphemeral(String path, String data) throws Exception
	{
		byte[] dataBuf = StringUtils.encodeUTF8(data);
		zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
			.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path, dataBuf);
	}
	
	public void delete(String path) throws Exception
	{
		zkClient.delete().forPath(path);
	}
	
	public boolean checkExist(String path) throws Exception
	{
		return zkClient.checkExists().forPath(path) != null;
	}
	
	public void setData(String path, String data) throws Exception
	{
		byte[] dataBuf = StringUtils.encodeUTF8(data);
		zkClient.setData().forPath(path, dataBuf);
	}
	
	public List<String> getChildren(String path, CuratorWatcher watcher) throws Exception
	{
		if(watcher == null)
		{
			return zkClient.getChildren().forPath(path);
		}
		else
		{
			return zkClient.getChildren().usingWatcher(watcher).forPath(path);
		}
	}
	
	public Map<String, String> getChildrenAndData(String path) throws Exception
	{
		Map<String, String> dataMap = new HashMap<String, String>();
		List<String> childrens = zkClient.getChildren().forPath(path);
		if(childrens != null && childrens.size() > 0)
		{
			for(String child : childrens)
			{
				String childPath = path + Constants.PATH_SEPARATOR + child;
				byte[] tmpdata = zkClient.getData().forPath(childPath);
				String childData = StringUtils.decodeUTF8(tmpdata);
				dataMap.put(childPath, childData);
			}
		}
		return dataMap;
	}
	
	public String getData(String path, CuratorWatcher watcher) throws Exception
	{
		if(watcher == null)
		{
			byte[] data = zkClient.getData().forPath(path);
			return StringUtils.decodeUTF8(data);
		}
		else
		{
			byte[] data = zkClient.getData().usingWatcher(watcher).forPath(path);
			return StringUtils.decodeUTF8(data);
		}
	}
	
	public void addConnLostListener(ZkConnLostListener listener)
	{
		if(!stateListeners.contains(listener))
		{
			stateListeners.add(listener);
		}
	}
	
	public static interface ZkConnLostListener
	{
		void sessionExpired();
	}
	
//	public void addReconnectWatcher(final String path, final ZookeeperWatcherType watcherType, final CuratorWatcher watcher)
//	{
//		synchronized(this)
//		{
//			log.info("Add new watcher " + watcher.toString() + ", for path " + path + " watcher type " + watcherType);
//			if(!watchers.contains(watcher.toString()))
//			{
//				zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener(){
//
//					@Override
//					public void stateChanged(CuratorFramework client,
//							ConnectionState newState) {
//						// TODO Auto-generated method stub
//						log.warn("Zk client connection state changed to " + newState);
//						if(newState == ConnectionState.LOST)// handle session expired event
//						{
//							try
//							{
//								if(watcherType == ZookeeperWatcherType.EXITS)
//								{
//									zkClient.checkExists().usingWatcher(watcher).forPath(path);
//								} 
//								else if(watcherType == ZookeeperWatcherType.GET_DATA)
//								{
//									zkClient.getData().usingWatcher(watcher).forPath(path);
//								}
//								else if(watcherType == ZookeeperWatcherType.GET_CHILDREN)
//								{
//									zkClient.getChildren().usingWatcher(watcher).forPath(path);
//								}
//								else if(watcherType == ZookeeperWatcherType.CREATE_ON_NO_EXITS)
//								{
//									Stat stat = zkClient.checkExists().usingWatcher(watcher).forPath(path);
//									if(stat == null)
//									{
//										log.info("Create path " + path + " because session expired occured");
//										zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
//										.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
//									}
//								}
//							}
//							catch(Exception e)
//							{
//								log.error(e.getMessage(), e);
//							}
//						}
//					}
//					
//				});
//			}
//		}
//	}
//	
//	public enum ZookeeperWatcherType{
//	     GET_DATA,GET_CHILDREN,EXITS,CREATE_ON_NO_EXITS
//	}
}
