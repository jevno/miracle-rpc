package com.miracle.module.rpc.loadbalance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;


public class LoadBalanceFactory {
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Map<String, LoadBalance> LOADBALANCES = new ConcurrentHashMap<String, LoadBalance>();
	
	private static class FactoryHolder
	{
		public static final LoadBalanceFactory FACTORY = new LoadBalanceFactory();
	}
	
	public static LoadBalanceFactory getInstance()
	{
		return FactoryHolder.FACTORY;
	}
	
	public LoadBalance getLoadBalance(RpcConfig config)
	{
		String lbKey = config.getParameter(Constants.LOAD_BALANCE_KEY, Constants.DEFAULT_LOAD_BALANCE);
		LOCK.lock();
		try
		{
			LoadBalance lb = LOADBALANCES.get(lbKey);
			if(lb != null)
				return lb;
			lb = createLoadBalance(lbKey);
			LOADBALANCES.put(lbKey, lb);
			return lb;
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	private LoadBalance createLoadBalance(String name)
	{
		if(name.equals(Constants.ROUNDROBIN_LOAD_BALANCE))
			return new RoundRobinLoadBalance();
		
		return new RoundRobinLoadBalance();
	}
}
