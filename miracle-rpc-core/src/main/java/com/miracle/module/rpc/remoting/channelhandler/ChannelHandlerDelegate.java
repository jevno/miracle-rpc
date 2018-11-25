package com.miracle.module.rpc.remoting.channelhandler;


public abstract class ChannelHandlerDelegate extends AbstractChannelInOutHandler{
	public abstract ChannelInOutHandler getChannelHandler();
}
