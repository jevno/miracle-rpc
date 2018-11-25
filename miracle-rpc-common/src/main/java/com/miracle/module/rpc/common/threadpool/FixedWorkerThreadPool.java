package com.miracle.module.rpc.common.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;

public class FixedWorkerThreadPool implements WorkerThreadPool{

	private static FixedWorkerThreadPool instance = new FixedWorkerThreadPool();
	private FixedWorkerThreadPool()
	{	
	}
	
	public static FixedWorkerThreadPool getInstance()
	{
		return instance;
	}
	
	@Override
	public Executor getExecutor(RpcConfig config, String name, int coreSize, int queueSize) {
        int threads = coreSize;
        int queues = queueSize;
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, 
        		queues == 0 ? new SynchronousQueue<Runnable>() : 
        			(queues < 0 ? new LinkedBlockingQueue<Runnable>() 
        					: new LinkedBlockingQueue<Runnable>(queues)),
        		new NamedThreadFactory(name, true), new AbortPolicyWithReport(name, config));
	}

}
