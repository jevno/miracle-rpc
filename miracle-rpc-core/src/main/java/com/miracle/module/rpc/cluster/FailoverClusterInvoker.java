package com.miracle.module.rpc.cluster;

import com.google.common.collect.Lists;
import com.miracle.common.miracle_utils.CollectionUtils;
import com.miracle.module.rpc.common.ExtensionLoader;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import com.miracle.module.rpc.directory.Directory;
import com.miracle.module.rpc.loadbalance.LoadBalance;
import org.apache.log4j.Logger;

import java.util.*;

public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {

	private static Logger log = Logger.getLogger(FailoverClusterInvoker.class);
	
	private List<ClusterRetryStrategy> retryStrategyList = null;
	
	public FailoverClusterInvoker(Directory<T> directory) {
		super(directory);
		
		Map<String, Class<?>> extensionClsMap = ExtensionLoader.getExtensionClss(ClusterRetryStrategy.class);
		if(extensionClsMap != null && extensionClsMap.size() > 0)
		{
			retryStrategyList = Lists.newArrayList();
			for(String extName : extensionClsMap.keySet())
			{
				List<Object> strategyList = ExtensionLoader.getExtensionInstances(
						ClusterRetryStrategy.class, extName);
				if(!CollectionUtils.isEmpty(strategyList))
				{
					for(Object strategy : strategyList)
					{
						retryStrategyList.add((ClusterRetryStrategy) strategy);
					}
				}
			}
		}
	}
	
	private boolean isNeedRetry(Invoker<T> invoker, RpcRequest rpcRequest, RpcException exp)
	{
		if(!CollectionUtils.isEmpty(retryStrategyList))
		{
			try
			{
				for(ClusterRetryStrategy retryStrategy : retryStrategyList)
				{
					if(!retryStrategy.isRetriable(invoker, rpcRequest, exp))
					{
						return false;
					}
				}
			}
			catch(Throwable t)
			{
				log.error("Unexpected exception while do cluster retrystrategy.", t);
			}
		}
		return true;
	}

	@Override
	protected RpcResult doInvoke(RpcRequest request, List<Invoker<T>> invokers, Invoker<T> grayInvoker,
                                 LoadBalance loadbalance) throws RpcException {

		List<Invoker<T>> copyinvokers = invokers;
		this.checkInvokers(copyinvokers, request);
		int tries = this.getConfig().getMethodParameter(request.getMethodName(), Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES);
		if(tries <= 0)
		{
			tries = 1;
		}
		List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(copyinvokers.size());
		Map<Invoker<T>, Integer> invokedErrCodeMap = new HashMap<Invoker<T>, Integer>(copyinvokers.size());
		Set<String> failedProviders = new HashSet<String>();
		for(int i=0; i<tries; i++)
		{
			if(i > 0)//重试时，进行重新选择，避免重试时invoker列表已发生变化.
			{
				this.checkWheatherDestoried();
				copyinvokers = this.list(request);
				grayInvoker = this.getGrayInvoker(copyinvokers);
				this.checkInvokers(copyinvokers, request);
			}
			Invoker<T> invoker = select(loadbalance, request, copyinvokers, grayInvoker, invoked);
			invoked.add(invoker);
			
			Integer lastInvokeErrCode = invokedErrCodeMap.get(invoker);
			if(lastInvokeErrCode != null)
			{
				if(lastInvokeErrCode == RpcException.TPSLIMIT_EXCEPTION || 
						lastInvokeErrCode == RpcException.EXECUTION_EXCEPTION ||
						lastInvokeErrCode == RpcException.DEGRADE_EXCEPTION ||
						lastInvokeErrCode == RpcException.NOSEMAPHORE_EXCEPTION ||
						lastInvokeErrCode == RpcException.RESPONSE_PACKET_TOO_LONG_EXCEPTION)
				{
					continue;
				}
			}
			
			try
			{
				RpcResult result = invoker.invoke(request);
				if(i > 0)
				{
					 log.warn("Although retry the method " + request.getMethodName()
	                            + " in the service " + request.getInterfaceName()
	                            + " was successful by the provider " + invoker.getConfig().getAddress()
	                            + ", but there have been failed providers " + failedProviders);
				}
				return result;
			}
			catch(Throwable t)
			{
				log.warn("Invoke " + request.getInterfaceName()
							+ " in the service " + request.getInterfaceName()
							+ " failed by the provider " + invoker.getConfig().getAddress(), t);
				
				if(t instanceof RpcException)
				{
					RpcException ex = (RpcException)t;
					int excCode = ex.getCode();
					
					if (excCode == RpcException.RESPONSE_PACKET_TOO_LONG_EXCEPTION) {
						log.error("case too long request is : " + request.toString());
					}
					
					invokedErrCodeMap.put(invoker, excCode);
					
					if(excCode == RpcException.LOCAL_DEGRADE_EXCEPTION || 
							excCode == RpcException.LOCAL_CIRCUITBREAKER_OPEN_EXCEPTION ||
							excCode == RpcException.RESPONSE_PACKET_TOO_LONG_EXCEPTION)
					{//消费端限流、降级、断路类异常不做failover，直接返回错误
						throw t;
					}
					
					if(!isNeedRetry(invoker, request, ex))
					{//deny by cluster retry strategy
						throw t;
					}
				}
			}
			finally
			{
				failedProviders.add(invoker.getConfig().getAddress());
			}
		}
		throw new RpcException("Failed to invoke the method "
	                + request.getMethodName() + " in the service " + getInterface().getName() 
	                + ". Tried " + tries + " times of the providers " + failedProviders 
	                );
	}

}
