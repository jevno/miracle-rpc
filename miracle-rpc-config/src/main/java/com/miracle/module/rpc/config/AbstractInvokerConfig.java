package com.miracle.module.rpc.config;

public class AbstractInvokerConfig extends AbstractConfig{
	protected Integer 	timeout;
	protected Integer 	retries;
	protected String 	loadBalance;
	protected String 	cluster;
	protected Boolean  	async;
	
	protected Integer	failThreshold;
	protected Integer   volumeThreshold;
	protected Integer   halfOpenTimeout;
	protected Integer	closeThreshold;
	protected Boolean	forceOpen;
	protected Boolean	forceClose;
	
	protected Boolean cache;
	protected Integer cacheSize;
	protected Integer cacheTtl;
	
	protected Boolean degrade;
	
    private Integer 	tps;
    private Long 		tpsInterval;
    
    private String		threadsName;
    private Integer     threadsCoreSize;
    private Integer 	threadsQueueSize;
    
    private Integer     semaphoreConcurrent;
	
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Integer getRetries() {
		return retries;
	}
	public void setRetries(Integer retries) {
		this.retries = retries;
	}
	public String getLoadBalance() {
		return loadBalance;
	}
	public void setLoadBalance(String loadBalance) {
		this.loadBalance = loadBalance;
	}
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	public Boolean getAsync() {
		return async;
	}
	public void setAsync(Boolean async) {
		this.async = async;
	}
	public Integer getFailThreshold() {
		return failThreshold;
	}
	public void setFailThreshold(Integer failThreshold) {
		this.failThreshold = failThreshold;
	}
	public Integer getFailCountWindow() {
		return volumeThreshold;
	}
	public void setFailCountWindow(Integer volumeThreshold) {
		this.volumeThreshold = volumeThreshold;
	}
	public Integer getHalfOpenTimeout() {
		return halfOpenTimeout;
	}
	public void setHalfOpenTimeout(Integer halfOpenTimeout) {
		this.halfOpenTimeout = halfOpenTimeout;
	}
	public Integer getCloseThreshold() {
		return closeThreshold;
	}
	public void setCloseThreshold(Integer closeThreshold) {
		this.closeThreshold = closeThreshold;
	}
	public Boolean getForceOpen() {
		return forceOpen;
	}
	public void setForceOpen(Boolean forceOpen) {
		this.forceOpen = forceOpen;
	}
	public Boolean getForceClose() {
		return forceClose;
	}
	public void setForceClose(Boolean forceClose) {
		this.forceClose = forceClose;
	}
	public Boolean getCache() {
		return cache;
	}
	public void setCache(Boolean cache) {
		this.cache = cache;
	}
	public Integer getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}
	public Integer getCacheTtl() {
		return cacheTtl;
	}
	public void setCacheTtl(Integer cacheTtl) {
		this.cacheTtl = cacheTtl;
	}
	public Boolean getDegrade() {
		return degrade;
	}
	public void setDegrade(Boolean degrade) {
		this.degrade = degrade;
	}
	public Integer getTps() {
		return tps;
	}
	public void setTps(Integer tps) {
		this.tps = tps;
	}
	public Long getTpsInterval() {
		return tpsInterval;
	}
	public void setTpsInterval(Long tpsInterval) {
		this.tpsInterval = tpsInterval;
	}
	public String getThreadsName() {
		return threadsName;
	}
	public void setThreadsName(String threadsName) {
		this.threadsName = threadsName;
	}
	public Integer getThreadsCoreSize() {
		return threadsCoreSize;
	}
	public void setThreadsCoreSize(Integer threadsCoreSize) {
		this.threadsCoreSize = threadsCoreSize;
	}
	public Integer getThreadsQueueSize() {
		return threadsQueueSize;
	}
	public void setThreadsQueueSize(Integer threadsQueueSize) {
		this.threadsQueueSize = threadsQueueSize;
	}
	public Integer getSemaphoreConcurrent() {
		return semaphoreConcurrent;
	}
	public void setSemaphoreConcurrent(Integer semaphoreConcurrent) {
		this.semaphoreConcurrent = semaphoreConcurrent;
	}
}
