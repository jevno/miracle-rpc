package com.miracle.module.rpc.config;

public class RegistryConfig extends AbstractConfig{
	private String address;
	private Integer sessconnTimeout;
	private Integer sessionTimeout;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getSessconnTimeout() {
		return sessconnTimeout;
	}
	public void setSessconnTimeout(Integer sessconnTimeout) {
		this.sessconnTimeout = sessconnTimeout;
	}
	public Integer getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	
}
