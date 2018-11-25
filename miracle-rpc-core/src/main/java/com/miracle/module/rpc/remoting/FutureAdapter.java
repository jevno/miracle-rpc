package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureAdapter<V> implements Future<V>{

	private final ResponseFuture future;
	
	public FutureAdapter(ResponseFuture future)
	{
		this.future = future;
	}
	
	public ResponseFuture getFuture()
	{
		return this.future;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return future.isDone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get() throws InterruptedException, ExecutionException {
		try{
			return (V)(((RpcResult)future.get()).recreate());
		}
		catch(RpcException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			throw new ExecutionException(t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		int timeoutInMillis = (int) unit.convert(timeout, TimeUnit.MILLISECONDS);
		
		try{
			return (V)(((RpcResult)future.get(timeoutInMillis)).recreate());
		}
		catch(RpcException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			throw new ExecutionException(t);
		}
	}

}
