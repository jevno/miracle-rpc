package com.miracle.module.rpc.cluster;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class ClusterFactory {
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Map<String, Cluster> CLUSTERS = new ConcurrentHashMap<String, Cluster>();
	
	private static class FactoryHolder
	{
		public static final ClusterFactory FACTORY = new ClusterFactory();
	}
	
	public static ClusterFactory getInstance()
	{
		return FactoryHolder.FACTORY;
	}
	
	public Cluster getCluster(RpcConfig config)
	{
		String clusterKey = config.getParameter(Constants.CLUSTER_KEY, Constants.DEFAULT_CLUSTER);
		LOCK.lock();
		try
		{
			Cluster cluster = CLUSTERS.get(clusterKey);
			if(cluster != null)
				return cluster;
			cluster = createCluster(clusterKey);
			CLUSTERS.put(clusterKey, cluster);
			return cluster;
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	private Cluster createCluster(String name)
	{
		if(name.equals(Constants.FAILOVER_CLUSTER))
			return new FailoverCluster();
		
		return new FailoverCluster();
	}
}
