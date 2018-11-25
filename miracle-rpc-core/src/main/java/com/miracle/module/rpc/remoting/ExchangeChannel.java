package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.RpcException;
import io.netty.channel.Channel;

public interface ExchangeChannel extends Channel {
	
	Channel getInnerChannel();
	/**
     * send request.
     * 
     * @param request
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request) throws RpcException;

    /**
     * send request.
     * 
     * @param request
     * @param timeout
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Object request, int timeout) throws RpcException;

    RpcConfig getConfig();
}
