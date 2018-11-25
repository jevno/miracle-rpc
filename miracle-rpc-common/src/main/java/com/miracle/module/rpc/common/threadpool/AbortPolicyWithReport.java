package com.miracle.module.rpc.common.threadpool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.miracle.module.rpc.common.RpcConfig;
import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.utils.Constants;

public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy{
	protected static final Logger logger = Logger.getLogger(AbortPolicyWithReport.class);
    
    private final String threadName;
    
    private final RpcConfig config;
    
    public AbortPolicyWithReport(String threadName, RpcConfig config) {
        this.threadName = threadName;
        this.config = config;
    }
    
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    	String host = this.config.getParameter(Constants.HOST_KEY);
    	int port = this.config.getParameter(Constants.PORT_KEY, 0);
        String msg = String.format("Thread pool is EXHAUSTED!" +
                " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s:%d!" ,
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(),
                host, port);
        logger.warn(msg);
        throw new RejectedExecutionException(msg);
    }
}
