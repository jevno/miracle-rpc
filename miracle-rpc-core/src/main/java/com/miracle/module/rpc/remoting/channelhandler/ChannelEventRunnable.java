package com.miracle.module.rpc.remoting.channelhandler;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;

public class ChannelEventRunnable implements Runnable{
	private static Logger log = Logger.getLogger(ChannelEventRunnable.class);
	private final ChannelInOutHandler handler;
	private final ChannelHandlerContext ctx;
	private final Object msg;
	
	public ChannelEventRunnable(ChannelInOutHandler handler, ChannelHandlerContext ctx, Object msg)
	{
		this.handler = handler;
		this.ctx = ctx;
		this.msg = msg;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try
		{
			handler.channelRead(ctx, msg);
		}
		catch(Exception e)
		{
			log.warn("ChannelEventRunnable handle channelRead, channel is " + ctx.channel()
                    + ", message is "+ msg, e);
		}
	}

}
