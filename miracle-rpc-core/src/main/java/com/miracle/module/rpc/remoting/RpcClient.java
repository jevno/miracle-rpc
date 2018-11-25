package com.miracle.module.rpc.remoting;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.Collection;

import com.miracle.module.rpc.common.RpcConfig;

public interface RpcClient {
	
	void close();
	
	RpcConfig getConfig();
	
	boolean isClosed();
	
	boolean isConnected();
	
	SocketAddress getConnectAddress();
	
	SocketAddress getRemoteAddress();
	
	SocketAddress getLocalAddress();
	
	ExchangeChannel getChannel();
	
	ResponseFuture request(Object req, int timeout);
	
	Collection<Channel> getChannels();
}
