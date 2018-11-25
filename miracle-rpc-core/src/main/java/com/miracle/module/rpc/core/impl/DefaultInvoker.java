package com.miracle.module.rpc.core.impl;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.AtomicPositiveInteger;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.remoting.FutureAdapter;
import com.miracle.module.rpc.remoting.ResponseFuture;
import com.miracle.module.rpc.remoting.RpcClient;
import com.miracle.module.rpc.core.api.*;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultInvoker<T> extends AbstractInvoker<T> {

	private final RpcClient[] clients;
	private final Set<Invoker<?>> invokers;
	private final ReentrantLock     destroyLock = new ReentrantLock();
	private final AtomicPositiveInteger index = new AtomicPositiveInteger();
	
	public DefaultInvoker(Class<T> type, RpcConfig config, RpcClient[] clients, Set<Invoker<?>> invokers) {
		super(type, config);
		this.clients = clients;
		this.invokers = invokers;
	}

	@Override
	protected RpcResult doInvoke(RpcRequest req) throws Throwable {
		// TODO Auto-generated method stub
		final String methodName = req.getMethodName();
		RpcClient currentclient;
		if(clients.length == 1)
		{
			currentclient = clients[0];
		}
		else
		{
			currentclient = clients[index.getAndIncrement() % clients.length];
		}
		
		int timeout = getConfig().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
		boolean isAsync = RpcUtils.isAsync(getConfig(), req);
		
		if(isAsync)
		{
			ResponseFuture future = currentclient.request(req, timeout);
			RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
			return new RpcResult();
		}
		else
		{
			RpcContext.getContext().setFuture(null);
			return (RpcResult) currentclient.request(req, timeout).get();
		}
	}

	@Override
    public boolean isAvailable()
	{
		if(!super.isAvailable())
		{
			return false;
		}
		for(RpcClient client: clients)
		{
			if(client.isConnected()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void destroy() {
		if(super.isDestroyed()) {
			return;
		}
		else
		{
			destroyLock.lock();
			try
			{
				if(super.isDestroyed())
				{
					return;
				}
				if(invokers != null)
				{
					invokers.remove(this);
				}
				for(RpcClient client : clients)
				{
					client.close();
				}
			}
			finally
			{
				destroyLock.unlock();
			}
		}
	}
}
