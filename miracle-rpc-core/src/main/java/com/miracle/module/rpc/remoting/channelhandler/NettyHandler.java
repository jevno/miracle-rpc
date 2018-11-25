package com.miracle.module.rpc.remoting.channelhandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.miracle.module.rpc.common.utils.NetUtils;

public class NettyHandler extends AbstractChannelInOutHandler{
	
	private ChannelInOutHandler channelHandler;
	private final Map<String/*ip_port*/, Channel> channels = new ConcurrentHashMap<String, Channel>();
	
	public NettyHandler(ChannelInOutHandler handler)
	{
		this.channelHandler = handler;
	}
	
	public Map<String, Channel> getChannels()
	{
		return channels;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String ipPort = NetUtils.toAddressString((InetSocketAddress)(ctx.channel().remoteAddress()));
		channels.put(ipPort, ctx.channel());
		channelHandler.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String ipPort = NetUtils.toAddressString((InetSocketAddress)(ctx.channel().remoteAddress()));
		channels.remove(ipPort);
		channelHandler.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		channelHandler.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		channelHandler.exceptionCaught(ctx, cause);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		String ipPort = NetUtils.toAddressString((InetSocketAddress)(ctx.channel().remoteAddress()));
		channels.remove(ipPort);
		channelHandler.close(ctx, promise);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		channelHandler.write(ctx, msg, promise);
	}

	@Override
	public void close()
	{
		if(this.channelHandler != null)
		{
			this.channelHandler.close();
		}
	}
}
