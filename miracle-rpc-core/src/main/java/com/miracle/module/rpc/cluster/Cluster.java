package com.miracle.module.rpc.cluster;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.directory.Directory;

public interface Cluster {
	
	<T> Invoker<T> join(Directory<T> directory) throws RpcException;
}
