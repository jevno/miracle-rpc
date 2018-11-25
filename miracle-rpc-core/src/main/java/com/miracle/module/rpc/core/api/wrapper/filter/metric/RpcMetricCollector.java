package com.miracle.module.rpc.core.api.wrapper.filter.metric;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import com.miracle.module.rpc.core.api.wrapper.filter.CircuitBreakerFilter;
import com.miracle.module.rpc.core.api.wrapper.filter.circuitbreaker.CircuitBreaker;
import com.miracle.module.rpc.remoting.DefaultFuture;
import com.miracle.module.rpc.remoting.RpcRequestProgressListener;
import org.apache.log4j.Logger;


public class RpcMetricCollector {
	
	private static Logger log = Logger.getLogger(RpcMetricCollector.class);
	
	private final ConcurrentMap<String, StatusRollingNumber> providerMethodMetrics = 
			new ConcurrentHashMap<String, StatusRollingNumber>();
	
	private final ConcurrentMap<String, StatusRollingNumber> consumerMethodMetrics = 
			new ConcurrentHashMap<String, StatusRollingNumber>();
	
	//circuit-breaker usage
	private final ConcurrentMap<String, StatusRollingNumber> methodHealthMetrics = 
			new ConcurrentHashMap<String, StatusRollingNumber>();
	
	private CircuitBreakerFilter cbFilter;

	private static final RpcMetricCollector KKRPC_METRICS = new RpcMetricCollector();
	
	private static final ScheduledExecutorService kkrpcMetricOutput = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactory(){

				@Override
				public Thread newThread(Runnable r) {
					String name = "KKrpcMetricWorker";
			        Thread ret = new Thread(r);
			        ret.setName(name);
			        ret.setDaemon(true);
			        return ret;
				}
				
			});
	static{
		DefaultFuture.registerProgressListener(new RpcRequestProgressListener() {

			@Override
			public void requestSubmitted(RpcRequest request) {
				KKRPC_METRICS.updateStatusForRequestSubmitted(request, false);
			}

			@Override
			public void requestFinished(RpcRequest request, RpcResult result, long elapsedInMs) {
				KKRPC_METRICS.updateStatusForRequestResponsed(request, result, elapsedInMs, false);
			}
			
		});
		
		kkrpcMetricOutput.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				try{
					if(KKRPC_METRICS.providerMethodMetrics != null && KKRPC_METRICS.providerMethodMetrics.size() > 0)
					{
						log.debug("Provider side: -------------------------------------");
						for(String key : KKRPC_METRICS.providerMethodMetrics.keySet())
						{
							StatusRollingNumber stat = KKRPC_METRICS.providerMethodMetrics.get(key);
							logMethodMetric(key, stat);
						}
					}
					if(KKRPC_METRICS.consumerMethodMetrics != null && KKRPC_METRICS.consumerMethodMetrics.size() > 0)
					{
						log.debug("Consumer side: -------------------------------------");
						for(String key : KKRPC_METRICS.consumerMethodMetrics.keySet())
						{
							StatusRollingNumber stat = KKRPC_METRICS.consumerMethodMetrics.get(key);
							logMethodMetric(key, stat);
						}
					}
				}
				catch(Throwable t)
				{
					log.error("Unexpected exception while output kkrpc metrics.", t);
				}
			}}, 60, 60, TimeUnit.SECONDS);
	}
	
	private static void logMethodMetric(String methodKey, StatusRollingNumber stat)
	{
		StatusRollingNumberEvent[] eventTypes = StatusRollingNumberEvent.values();
		StringBuilder sb = new StringBuilder();
		sb.append(methodKey).append("===>{");
		boolean bFirst = true;
		for(StatusRollingNumberEvent event: eventTypes)
		{
			if(bFirst)
			{
				bFirst = false;
			}
			else
			{
				sb.append(", ");
			}
			sb.append(event.name());
			sb.append("={");
			sb.append(Arrays.toString(stat.getValues(event)));
			sb.append("}");
		}
		sb.append("}\n");
		log.debug(sb.toString());
	}
	
	
	public static RpcMetricCollector getInstance()
	{
		return KKRPC_METRICS;
	}
	
	public void registerCircuitBreakerFilter(CircuitBreakerFilter cbFilter)
	{
		this.cbFilter = cbFilter;
	}
	
	public void updateStatusForRequestSubmitted(RpcRequest request, boolean bProviderSide)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProviderSide);
		stat.add(StatusRollingNumberEvent.COMMAND_CONCURRENT, 1);
		stat.updateRollingMax(StatusRollingNumberEvent.COMMAND_MAX_CONCURRENT, 
				stat.getValueOfLatestBucket(StatusRollingNumberEvent.COMMAND_CONCURRENT));
	}
	
	public void updateStatusForRequestResponsed(RpcRequest request, RpcResult result, long duration, boolean bProviderSide)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProviderSide);
		stat.add(StatusRollingNumberEvent.COMMAND_CONCURRENT, -1);
		if(result != null && !result.hasException())
		{
			stat.increment(StatusRollingNumberEvent.SUCCESS);
			stat.add(StatusRollingNumberEvent.SUCCESS_ELAPSED, duration);
		}
		else if(result != null && result.getException() != null)
		{
			stat.increment(StatusRollingNumberEvent.FAILURE);
			stat.add(StatusRollingNumberEvent.FAILED_ELAPSED, duration);
			
			if(result.getException() instanceof RpcException)
			{
				RpcException ex = (RpcException)result.getException();
				if(ex.isTimeout())
				{
					stat.increment(StatusRollingNumberEvent.TIMEOUT);
				}
			}
		}
		
		//if consumer side do health check for circuit-breaker
		if(!bProviderSide)
		{
			StatusRollingNumber healthStat = this.getMethodHealthMetric(request);
			if(result != null && result.getException() != null && result.getException() instanceof RpcException)
			{
				RpcException ex = (RpcException)result.getException();
				if(!ex.isIgnorableExceptionForErrorStat())
				{
					healthStat.increment(StatusRollingNumberEvent.FAILURE);
				}
				else
				{
					healthStat.increment(StatusRollingNumberEvent.SUCCESS);
					markCircuitBreakerFilterSuccess(request);
				}
				
				if(ex.isTimeout())
				{
					this.markResponseTimeout(request);
					healthStat.increment(StatusRollingNumberEvent.TIMEOUT);
				}
			}
			else
			{
				healthStat.increment(StatusRollingNumberEvent.SUCCESS);
				markCircuitBreakerFilterSuccess(request);
			}
		}
	}
	
	private void markCircuitBreakerFilterSuccess(RpcRequest request)
	{
		if(this.cbFilter != null)
		{
			CircuitBreaker cb = this.cbFilter.getCircuitBreaker(request);
			if(cb != null)
			{
				cb.markSuccess(request);
			}
		}
	}
	
	public long getLatestCommandMaxConcurrent(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		return stat.getValueOfLatestBucket(StatusRollingNumberEvent.COMMAND_MAX_CONCURRENT);
	}
	
	public void markResponseTimeout(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodMetric(request, false);
		stat.increment(StatusRollingNumberEvent.TIMEOUT);
	}
	
	public void markResponseFromCache(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		stat.increment(StatusRollingNumberEvent.RESPONSE_FROM_CACHE);
	}
	
	public void markProviderTargetExceptionThrown(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodMetric(request, true);
		stat.increment(StatusRollingNumberEvent.EXCEPTION_THROWN);
	}
	
	public void markDegraded(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		stat.increment(StatusRollingNumberEvent.DEGRADED);
	}
	
	public void markTpsLimited(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		stat.increment(StatusRollingNumberEvent.TPS_LIMITED);
	}
	
	public void markProviderThreadPoolRejected(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodMetric(request, true);
		stat.increment(StatusRollingNumberEvent.THREAD_POOL_REJECTED);
	}
	
	public void markSemaphoreRejected(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		stat.increment(StatusRollingNumberEvent.SEMAPHORE_REJECTED);
	}
	
	public void markShortCircuited(RpcRequest request, boolean bProvider)
	{
		StatusRollingNumber stat = getMethodMetric(request, bProvider);
		stat.increment(StatusRollingNumberEvent.SHORT_CIRCUITED);
	}
	
	private StatusRollingNumber getMethodMetric(RpcRequest request, boolean bProvider)
	{
		String key = request.getInterfaceName() + "_" + request.getMethodName();
		StatusRollingNumber metric = null;
		if(bProvider)
		{
			metric = providerMethodMetrics.get(key);
			if(metric == null)
			{
				providerMethodMetrics.putIfAbsent(key, new StatusRollingNumber(60*60*1000, 60));
				metric = providerMethodMetrics.get(key);
			}
		}
		else
		{
			metric = consumerMethodMetrics.get(key);
			if(metric == null)
			{
				consumerMethodMetrics.putIfAbsent(key, new StatusRollingNumber(60*60*1000, 60));
				metric = consumerMethodMetrics.get(key);
			}
		}
		return metric;
	}
	
	public long getHealthMetricSuccCnt(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodHealthMetric(request);
		return stat.getValueOfLatestBucket(StatusRollingNumberEvent.SUCCESS);
	}
	
	public long getHealthMetricFailCnt(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodHealthMetric(request);
		return stat.getValueOfLatestBucket(StatusRollingNumberEvent.FAILURE);
	}
	
	public void resetHealthMetric(RpcRequest request)
	{
		StatusRollingNumber stat = getMethodHealthMetric(request);
		stat.reset();
	}
	
	private StatusRollingNumber getMethodHealthMetric(RpcRequest request)
	{
		String key = request.getInterfaceName() + "_" + request.getMethodName();
		StatusRollingNumber metric = methodHealthMetrics.get(key);
		if(metric == null)
		{
			methodHealthMetrics.putIfAbsent(key, new StatusRollingNumber(60*1000, 6));
			metric = methodHealthMetrics.get(key);
		}
		return metric;
	}
	
	public void reset()
	{
		providerMethodMetrics.clear();
		consumerMethodMetrics.clear();
		methodHealthMetrics.clear();
		cbFilter = null;
	}
}
