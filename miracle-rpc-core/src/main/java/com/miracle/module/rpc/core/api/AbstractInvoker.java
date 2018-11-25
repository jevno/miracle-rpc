package com.miracle.module.rpc.core.api;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.RpcConfig;


public abstract class AbstractInvoker<T> implements Invoker<T>{
	
	protected final Logger   logger    = Logger.getLogger(AbstractInvoker.class);
	
	private final Class<T> type;
	
	private final RpcConfig config;
	
	private volatile boolean available = true;
	private volatile boolean destroyed = false;
	
	@Override
	public Class<T> getInterface() {
		return type;
	}
	
	public AbstractInvoker(Class<T> type, RpcConfig config) {
        if (type == null) {
			throw new IllegalArgumentException("service type == null");
		}
        this.type = type;
        this.config = config;
    }
	
	@Override
	public String toString() {
        return "" + getInterface();
    }
	
	@Override
	public RpcConfig getConfig()
	{
		return config;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}
	    
	protected void setAvailable(boolean available) {
	    this.available = available;
	}

    @Override
	public void destroy() {
        if (isDestroyed()) {
            return;
        }
        destroyed = true;
        setAvailable(false);
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
	    
	//Tracing时，本地RPC调用不会经过这里
    @Override
	public RpcResult invoke(RpcRequest request) throws RpcException {
        RpcResult ret = null;
        int id = request.id;
        
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if(attachments != null && attachments.size() > 0)
        {
        	for(Map.Entry<String, String> entry : attachments.entrySet())
        	{
        		request.addAttachmentIfAbsent(entry.getKey(), entry.getValue());
        	}
        }
        
        try {
        	ret = doInvoke(request);
        } catch (InvocationTargetException e) { // biz exception       
        	if(request.getSerializeType() == RpcHeader.CODEC_PROTOSTUFF) //java client request
        	{
	            Throwable te = e.getTargetException();
	            if (te == null) {
	                return new RpcResult(id, request.getSerializeType(), e);
	            } else {
	                if (te instanceof RpcException) {
	                    ((RpcException) te).setCode(RpcException.BIZ_EXCEPTION);
	                }
	                return new RpcResult(id, request.getSerializeType(), te);
	            }
        	}
        	else
        	{
        		Throwable te = e.getTargetException();
 	            if (te == null) {
 	                return new RpcResult(id, request.getSerializeType(), new RpcProtoError(e.getClass().getName(), e.getMessage()));
 	            } else {
 	            	return new RpcResult(id, request.getSerializeType(), new RpcProtoError(te.getClass().getName(), te.getMessage()));
 	            }
        	}
        } catch (RpcException e) {        	
        	if(request.getSerializeType() == RpcHeader.CODEC_PROTOSTUFF)
        	{
	            if (e.isBiz()) {
	                return new RpcResult(id, request.getSerializeType(), e);
	            } else {
	                throw e;
	            }
        	}
        	else
        	{
        		return new RpcResult(id, request.getSerializeType(), new RpcProtoError(e.getClass().getName(), e.getMessage()));
        	}
        } catch (Throwable e) {
        	if(request.getSerializeType() == RpcHeader.CODEC_PROTOSTUFF)
        	{
        		return new RpcResult(id, request.getSerializeType(), e);
        	}
        	else
        	{
        		return new RpcResult(id, request.getSerializeType(), new RpcProtoError(e.getClass().getName(), e.getMessage()));
        	}
        } 
        
        return ret;
    }

    protected abstract RpcResult doInvoke(RpcRequest req) throws Throwable;
}
