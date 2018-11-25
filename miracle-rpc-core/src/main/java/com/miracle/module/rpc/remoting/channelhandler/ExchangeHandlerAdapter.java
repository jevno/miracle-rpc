package com.miracle.module.rpc.remoting.channelhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.apache.log4j.Logger;

public abstract class ExchangeHandlerAdapter extends AbstractChannelInOutHandler implements ExchangeHandler{

	private final static Logger log = Logger.getLogger(ExchangeHandlerAdapter.class);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		log.warn("Do nothings, should not be here");
	}

	@Override
	public void close()
	{
		//do nothing
	}
}
