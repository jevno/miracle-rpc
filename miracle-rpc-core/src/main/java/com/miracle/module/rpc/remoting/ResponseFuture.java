package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.core.api.RpcException;

public interface ResponseFuture {
	/**
     * get result.
     * 
     * @return result.
     */
    Object get() throws RpcException;

    /**
     * get result with the specified timeout.
     * 
     * @param timeoutInMillis timeout.
     * @return result.
     */
    Object get(int timeoutInMillis) throws RpcException;


    /**
     * check is done.
     * 
     * @return done or not.
     */
    boolean isDone();
}
