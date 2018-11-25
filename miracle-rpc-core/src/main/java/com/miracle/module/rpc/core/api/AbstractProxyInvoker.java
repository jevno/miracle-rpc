package com.miracle.module.rpc.core.api;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import com.google.common.base.Throwables;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;


public abstract class AbstractProxyInvoker<T> implements Invoker<T> {

	private static Logger log = Logger.getLogger(AbstractProxyInvoker.class);
	
	private final T proxy;
	    
	private final Class<T> type;
	
	private final RpcConfig config;
	    

	public AbstractProxyInvoker(T proxy, Class<T> type, RpcConfig config){
	    if (proxy == null) {
	        throw new IllegalArgumentException("proxy == null");
	    }
	    if (type == null) {
	        throw new IllegalArgumentException("interface == null");
	    }
	    if (! type.isInstance(proxy)) {
	        throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface " + type);
	    }
	    this.proxy = proxy;
	    this.type = type;
	    this.config = config;
	}

    @Override
	public Class<T> getInterface() {
        return type;
    }

    @Override
	public RpcConfig getConfig() {
        return config;
    }

    @Override
	public boolean isAvailable() {
        return true;
    }
    
    @Override
	public void destroy() {
    }

    @Override
	public RpcResult invoke(RpcRequest req) throws RpcException {
    	//ps:对本地rpc调用来说由于不经过AbstractInvoker.invoker, 那么req中不存在tracingId，所以本地rpc不会tracing。
    	int id = req.getId();
        try {
            return new RpcResult(id, req.getSerializeType(), doInvoke(proxy, req.getMethodName(), req.getArguments()));
        } catch (InvocationTargetException e) {
        	RpcMetricCollector.getInstance().markProviderTargetExceptionThrown(req);
        	Throwable te = e.getTargetException();
        	if(te != null)
        	{

        		log.warn("AbstractProxyInvoker InvocationTargetException te:" + te.getClass().getName() +
        				", message:" + te.getMessage() + ", details:" + Throwables.getStackTraceAsString(te));
        		
        		if(te instanceof RpcException)
        		{
        			RpcException ex = (RpcException)te;
        			ex.setCode(RpcException.BIZ_EXCEPTION);
        		}
        	}
        	else 
        	{
        		log.error("AbstractProxyInvoker InvocationTargetException e:" + e.getMessage(), e);
        	}
        	
        	if(req.getSerializeType() == RpcHeader.CODEC_PROTOSTUFF)
        	{
	        	if(te != null)
	        	{
	        		return new RpcResult(id, req.getSerializeType(), te);
	        	}
	        	else
	        	{
	        		return new RpcResult(id, req.getSerializeType(), e);
	        	}
        	}
        	else
        	{
        		if(te == null)
        		{
        			return new RpcResult(id, req.getSerializeType(), new RpcProtoError(e.getClass().getName(), e.getMessage()));
        		}
        		else
        		{
        			return new RpcResult(id, req.getSerializeType(), new RpcProtoError(te.getClass().getName(), te.getMessage()));
        		}
        	}
        } catch (Throwable e) {
        	log.error("AbstractProxyInvoker uncatched exception:" + e.getMessage(), e);
        	if(req.getSerializeType() == RpcHeader.CODEC_PROTOSTUFF)
        	{
        		throw new RpcException("Failed to invoke proxy method " + req.getMethodName() + ", cause: " + e.getMessage(), e);
        	}
        	else
        	{
        		return new RpcResult(id, req.getSerializeType(), new RpcProtoError(e.getClass().getName(), e.getMessage()));
        	}
        }
    }
    
    protected abstract Object doInvoke(T proxy, String methodName, Object[] arguments) throws Throwable;

    @Override
    public String toString() {
        return "" + getInterface();
    }
}
