package com.miracle.module.rpc.core.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class RpcContext {

	private static final ThreadLocal<RpcContext> LOCAL = new ThreadLocal<RpcContext>(){
		 protected RpcContext initialValue()
		 {
			 return new RpcContext();
		 }
	};
	
	public static RpcContext getContext()
	{
		return LOCAL.get();
	}
	
	public static void removeContext()
	{
		LOCAL.remove();
	}
	
	private Future<?> future;
	
	private boolean bAsync = false;
	
	private InetSocketAddress remoteAddress;
	
	private final Map<String, String> attachments = new HashMap<String, String>();
	
	protected RpcContext(){
	}
	
    @SuppressWarnings("unchecked")
    public <T> Future<T> getFuture() {
        return (Future<T>) future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }
    
    public boolean isAsyncMode()
    {
    	return bAsync;
    }
    
    public void asyncBegin()
    {
    	bAsync = true;
    }
    
    public void asyncEnd()
    {
    	bAsync = false;
    }

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	public String getRemoteHost()
	{
		return remoteAddress == null? null : remoteAddress.getAddress().getHostAddress();
	}
	
	public int getRemotePort()
	{
		return remoteAddress == null? 0 : remoteAddress.getPort();
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
    
    public String getAttachment(String key)
    {
    	return this.attachments.get(key);
    }
    
    public RpcContext setAttachment(String key, String val)
    {
    	if(val == null)
    	{
    		this.attachments.remove(key);
    	}
    	else
    	{
    		this.attachments.put(key, val);
    	}
    	return this;
    }
    
    public RpcContext removeAttachment(String key)
    {
    	this.attachments.remove(key);
    	return this;
    }
    
    public Map<String, String> getAttachments()
    {
    	return this.attachments;
    }
    
    public RpcContext setAttachments(Map<String, String> attachment)
    {
    	this.attachments.clear();
    	if(attachment != null && attachment.size() > 0)
    	{
    		this.attachments.putAll(attachment);
    	}
    	
    	return this;
    }
    
    public void clearAttachments()
    {
    	this.attachments.clear();
    }
    
}
