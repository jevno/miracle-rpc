package com.miracle.module.rpc.core.api.wrapper.filter.circuitbreaker;


public class CircuitBreakerConfig {

    //closed状态的失败次数阈值
    private final int failPercentThreshold;

    //closed状态的失败计数的时间窗口
    private final int volumeThreshold;

    //处于open状态下进入half-open的超时时间
    private final int open2HalfOpenTimeoutInMs;

    //half-open状态下成功次数阈值
    private final int closeSuccThreshold;
    
    private final boolean forceOpen;
    
    private final boolean forceClose;

    public CircuitBreakerConfig(int failPercentThreshold, int volumeThreshold, 
    		int open2HalfOpenTimeoutInMs, int closeSuccThreshold, boolean forceOpen, boolean forceClose){
    	this.volumeThreshold = volumeThreshold;
    	this.failPercentThreshold = failPercentThreshold;
    	this.open2HalfOpenTimeoutInMs = open2HalfOpenTimeoutInMs;
    	this.closeSuccThreshold = closeSuccThreshold;
    	this.forceOpen = forceOpen;
    	this.forceClose = forceClose;
    }

    public int getFailThreshold() {
        return failPercentThreshold;
    }

    public int getVolumeThreshold() {
		return volumeThreshold;
	}

	public int getOpen2HalfOpenTimeoutInMs() {
        return open2HalfOpenTimeoutInMs;
    }


    public int getCloseSuccThreshold() {
        return this.closeSuccThreshold;
    }

    public boolean isForceOpen()
    {
    	return this.forceOpen;
    }
    
    public boolean isForceClose()
    {
    	return this.forceClose;
    }
}
