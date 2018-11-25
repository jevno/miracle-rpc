package com.miracle.module.rpc.common.threadpool;

import java.util.concurrent.Executor;

import com.miracle.module.rpc.common.RpcConfig;

public interface WorkerThreadPool {
	Executor getExecutor(RpcConfig config, String name, int coreSize, int queueSize);
}
