package com.miracle.module.rpc.remoting;

import io.netty.channel.Channel;

import java.util.Collection;

import com.miracle.module.rpc.common.RpcConfig;

public interface RpcServer {
	String getServerHost();
	
	int getServerPort();
	
	RpcConfig getConfig();
	
	Collection<Channel> getChannels();
	
	void close();
}
