package com.miracle.module.rpc.config;

public class AbstractReferenceConfig extends InterfaceConfig{
	protected String version;
	// 连接数限制,0表示共享连接，否则为该服务独享连接数
    protected Integer connections;
    protected Boolean sticky;
    protected Integer reconnect;
    protected String host;
    protected Integer port;
    protected Integer connectTimeout;
    protected String iprouter;
	private Boolean injvm;
	private Boolean init;
    
	
	public Boolean getInjvm() {
		return injvm;
	}
	public void setInjvm(Boolean injvm) {
		this.injvm = injvm;
	}
	public Boolean getInit() {
		return init;
	}
	public void setInit(Boolean init) {
		this.init = init;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Integer getConnections() {
		return connections;
	}
	public void setConnections(Integer connections) {
		this.connections = connections;
	}
	public Boolean isSticky() {
		return sticky;
	}
	public void setSticky(Boolean sticky) {
		this.sticky = sticky;
	}
	public Integer getReconnect() {
		return reconnect;
	}
	public void setReconnect(Integer reconnect) {
		this.reconnect = reconnect;
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
	public Integer getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public String getIprouter() {
		return iprouter;
	}
	public void setIprouter(String iprouter) {
		this.iprouter = iprouter;
	}
}
