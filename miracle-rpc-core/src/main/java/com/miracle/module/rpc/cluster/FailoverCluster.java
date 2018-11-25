package com.miracle.module.rpc.cluster;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.directory.Directory;

public class FailoverCluster implements Cluster{

	@Override
	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new FailoverClusterInvoker<T>(directory);
	}

}
