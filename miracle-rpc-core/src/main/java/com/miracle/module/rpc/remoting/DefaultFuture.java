package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture implements ResponseFuture{
	private static Logger log = Logger.getLogger(DefaultFuture.class);
	private static final Map<Integer, DefaultFuture> FUTURES = new ConcurrentHashMap<Integer, DefaultFuture>();
	private static List<RpcRequestProgressListener> progressListeners =
			new CopyOnWriteArrayList<RpcRequestProgressListener>();
	private final int id;
	private final RpcRequest request;
	private final ExchangeChannel channel;
	private final int timeout;
	
	private final Lock lock = new ReentrantLock();
	private final Condition done = lock.newCondition();
	
	private final long start = System.currentTimeMillis();
	private volatile long sent;
	private volatile RpcResult response;
	
	public DefaultFuture(ExchangeChannel channel, RpcRequest request, int timeout)
	{
		this.channel = channel;
		this.request = request;
		this.timeout = timeout > 0? timeout : channel.getConfig().getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
		this.id = request.getId();
		FUTURES.put(this.id, this);
		fireRequestSubmitted(request);
	}
	
	public static void registerProgressListener(RpcRequestProgressListener listener)
	{
		if(listener != null && !progressListeners.contains(listener))
		{
			progressListeners.add(listener);
		}
	}
	
	public static void unregisterProgressListener(RpcRequestProgressListener listener)
	{
		if(listener != null)
		{
			progressListeners.remove(listener);
		}
	}
	
	public static void fireRequestSubmitted(RpcRequest request)
	{
		if(progressListeners.size() > 0)
		{
			for(RpcRequestProgressListener listener : progressListeners)
			{
				listener.requestSubmitted(request);
			}
		}
	}
	
	public static void fireRequestFinished(RpcRequest request, RpcResult response, long duration)
	{
		if(progressListeners.size() > 0)
		{
			for(RpcRequestProgressListener listener : progressListeners)
			{
				listener.requestFinished(request, response, duration);
			}
		}
	}
	
	@Override
	public Object get() throws RpcException {
		// TODO Auto-generated method stub
		return get(timeout);
	}
	
	private String getTimeoutMessage(boolean scan) 
	{
        long nowTimestamp = System.currentTimeMillis();
        return (sent > 0 ? "Waiting server-side response timeout" : "Sending request timeout in client-side")
                    + (scan ? " by scan timer" : "") + ". start time: " 
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time: " 
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
                    + (sent > 0 ? " client elapsed: " + (sent - start) 
                        + " ms, server elapsed: " + (nowTimestamp - sent)
                        : " elapsed: " + (nowTimestamp - start)) + " ms, timeout: "
                    + timeout + " ms, request: " + request + ", channel: " + channel.localAddress()
                    + " -> " + channel.remoteAddress();
	}
	
	@Override
	public Object get(int timeoutInMillis) throws RpcException {
		if(!isDone())
		{
			long start = System.currentTimeMillis();
			lock.lock();
			try{
				while(!isDone())
				{
					done.await(timeout, TimeUnit.MILLISECONDS);
					if(isDone() || System.currentTimeMillis() - start > timeout)
						break;
				}
			}
			catch(InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			finally 
			{
				lock.unlock();
			}
			if(!isDone())
			{
				RpcException timeoutException = new RpcException(RpcException.TIMEOUT_EXCEPTION, getTimeoutMessage(false));
				fireRequestFinished(request, new RpcResult(request.getId(), request.getSerializeType(), timeoutException), System.currentTimeMillis()-start);
				throw timeoutException;
			}
		}
		
		if(this.response == null)
		{
			return new IllegalStateException("response cannot be null");
		}
		else if(this.response.getException() != null)
		{
			if((this.response.getException() instanceof RpcException) 
					&& ((RpcException)this.response.getException()).getCode() != RpcException.BIZ_EXCEPTION)
				throw (RpcException)this.response.getException();
		}
		
		return this.response;
	}
	
	public void cancel()
	{
		RpcResult result = new RpcResult();
		result.setId(id);
		result.setException(new RpcException(RpcException.CANCELLED_EXCEPTION, "RpcRequest has been cancelled."));
		this.response = result;
		FUTURES.remove(id);
	}
	
	public static DefaultFuture getFuture(int id)
	{
		return FUTURES.get(id);
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return this.response != null;
	}

	public static void sent(Channel channel, RpcRequest request)
	{
		DefaultFuture future = FUTURES.get(request.getId());
	    if (future != null) {
	    	future.sent = System.currentTimeMillis();
	    }
	}
	
	private void doReceived(RpcResult res)
	{
		lock.lock();
		try{
			this.response = res;
			if(done != null)
			{
				done.signal();
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public static void received(Channel channel, RpcResult response)
	{
		try
		{
			DefaultFuture future = FUTURES.get(response.getId());
			if(future != null)
			{
				future.doReceived(response);
				fireRequestFinished(future.request, response, System.currentTimeMillis()-future.start);
			}
			else
			{//maybe The DefaultFuture had been removed by RpcInvocationTimeoutScan
				log.warn("The timeout response finally returned at " 
	                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) 
	                    + ", response " + response 
	                    + (channel == null ? "" : ", channel: " + channel.localAddress() 
	                        + " -> " + channel.remoteAddress()));
			}
		}
		finally
		{
			FUTURES.remove(response.getId());
		}
	}
	
	private static class RpcInvocationTimeoutScan implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				try
				{
					for(DefaultFuture future : FUTURES.values())
					{
						if(future == null || future.isDone())
							continue;
						if(System.currentTimeMillis() - future.start > future.timeout)
						{
							RpcResult timeoutResponse = new RpcResult();
							timeoutResponse.setId(future.request.getId());
							timeoutResponse.setException(new RpcException(RpcException.TIMEOUT_EXCEPTION, future.getTimeoutMessage(true)));
							DefaultFuture.received(future.channel, timeoutResponse);
						}
					}
					Thread.sleep(50);
				}
				catch(Throwable t)
				{
					log.error("Exception when scan the timeout invocation of remoting.", t);
				}
			}
		}
	}
	
	static {
		Thread th = new Thread(new RpcInvocationTimeoutScan(), "RpcResponseTimeoutScanTask");
		th.setDaemon(true);
		th.start();
	}
}
