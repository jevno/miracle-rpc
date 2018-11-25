package com.miracle.module.rpc.core.api.wrapper.filter.circuitbreaker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;
import org.apache.log4j.Logger;


public class CircuitBreaker {

    private static final Logger logger = Logger.getLogger(CircuitBreaker.class);

    private String name;
    
    private CircuitBreakerConfig config;

    private AtomicBoolean circuitOpen;
    
    private AtomicLong circuitOpenedOrLastTestedTime;

    //构造器
    public CircuitBreaker(String name, CircuitBreakerConfig config) {
    	this.name = name;
        this.config = config;
        this.circuitOpen = new AtomicBoolean(false);
        this.circuitOpenedOrLastTestedTime = new AtomicLong(0l);
    }

    //状态判断
    public boolean isOpen(RpcRequest request){
    	if(circuitOpen.get())
    	{
    		return true;
    	}
    	if(isOpenThresholdReached(request))
    	{
    		if(this.circuitOpen.compareAndSet(false, true))
    		{
    			 this.circuitOpenedOrLastTestedTime.set(System.currentTimeMillis());
    			 logger.warn("Circuit open now: " + name);
    		}
    		return true;
    	}
    	return false;
    }
    
    public void markSuccess(RpcRequest request)
    {
    	if(circuitOpen.get())
    	{
    		if(circuitOpen.compareAndSet(true, false))
    		{
    			RpcMetricCollector.getInstance().resetHealthMetric(request);
    			logger.info("Circuit close now: " + name);
    		}
    	}
    }
    
    private boolean isOpenThresholdReached(RpcRequest request){
    	long succeed = RpcMetricCollector.getInstance().getHealthMetricSuccCnt(request);
    	long failed = RpcMetricCollector.getInstance().getHealthMetricFailCnt(request);
    	long total = succeed + failed;
    	if(total < config.getVolumeThreshold()) {
			return false;
		}
    	
    	int failedPercent = (int) ((failed * 100) / total); 
    	if(failedPercent >= this.config.getFailThreshold())
    		return true;
    	return false;
    }

    public boolean allowSingleTest() {
    	long timeCircuitOpenedOrWasLastTested = circuitOpenedOrLastTestedTime.get();
    	if (circuitOpen.get() && System.currentTimeMillis() > (timeCircuitOpenedOrWasLastTested + config.getOpen2HalfOpenTimeoutInMs()))
    	{
    		if(circuitOpenedOrLastTestedTime.compareAndSet(timeCircuitOpenedOrWasLastTested, System.currentTimeMillis()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
   
    public boolean allowRequest(RpcRequest request)
    {
    	if(config.isForceOpen()){
    		return false;
    	}
    	if(config.isForceClose()){
    		return true;
    	}
    	return !this.isOpen(request) || allowSingleTest();
    }
}
