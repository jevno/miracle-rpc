package com.miracle.module.rpc.core.api;

import com.miracle.module.rpc.common.RpcConfig;

public interface Invoker<T> {

	RpcConfig getConfig();
	
	boolean isAvailable();
	
	void destroy();
	
    /**
     * get service interface.
     * 
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     * 
     * @param request
     * @return result
     * @throws RpcException
     */
    RpcResult invoke(RpcRequest request) throws RpcException;

}
