package com.miracle.module.rpc.remoting.channelhandler;


import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

public interface ChannelInOutHandler extends ChannelInboundHandler, ChannelOutboundHandler {
	void close();
}
