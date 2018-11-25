package com.miracle.module.rpc.config;

public class AbstractServiceConfig extends InterfaceConfig{
	protected String version;
	protected String host;
	protected Integer port;
	protected Integer workerThreads;
	protected Integer ioThreads;
	protected Integer queueSize;	
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Integer getWorkerThreads() {
		return workerThreads;
	}
	public void setWorkerThreads(Integer workerThreads) {
		this.workerThreads = workerThreads;
	}
	public Integer getIoThreads() {
		return ioThreads;
	}
	public void setIoThreads(Integer ioThreads) {
		this.ioThreads = ioThreads;
	}
	public Integer getQueueSize() {
		return queueSize;
	}
	public void setQueueSize(Integer queueSize) {
		this.queueSize = queueSize;
	}
}
