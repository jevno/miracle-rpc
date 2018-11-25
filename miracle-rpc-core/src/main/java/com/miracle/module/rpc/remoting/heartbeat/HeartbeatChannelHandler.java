package com.miracle.module.rpc.remoting.heartbeat;

import com.miracle.module.rpc.core.api.RpcObject;
import com.miracle.module.rpc.remoting.channelhandler.ChannelHandlerDelegate;
import com.miracle.module.rpc.remoting.channelhandler.ChannelInOutHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.log4j.Logger;

public class HeartbeatChannelHandler extends ChannelHandlerDelegate {
	private static final Logger log = Logger.getLogger(HeartbeatChannelHandler.class);
	protected final ChannelInOutHandler channellHandler;
	
	public HeartbeatChannelHandler(ChannelInOutHandler handler)
	{
		this.channellHandler = handler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		HeartbeatChannelHelper.setReadTimestamp(ctx.channel());
		this.channellHandler.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		HeartbeatChannelHelper.clearReadTimestamp(ctx.channel());
		this.channellHandler.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		HeartbeatChannelHelper.setReadTimestamp(ctx.channel());
		if(HeartbeatUtil.isHeartbeatRequest(msg))
		{
			HeartbeatChannelHelper.setWriteTimestamp(ctx.channel());
			ctx.writeAndFlush(HeartbeatUtil.getDefaultHeartbeatResult(((RpcObject)msg).getSerializeType()));
			if(log.isDebugEnabled())
			{
				 log.debug("Received heartbeat from remote channel and ack back " + ctx.channel());
			}
			return;
		}
		if(HeartbeatUtil.isHeartbeatResponse(msg))
		{
		 	if (log.isDebugEnabled()) {
		 		log.debug("Receive heartbeat response from channel " + ctx.channel());
            }
		 	return;
		}
		this.channellHandler.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		this.channellHandler.exceptionCaught(ctx, cause);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		HeartbeatChannelHelper.clearWriteTimestamp(ctx.channel());
		this.channellHandler.close(ctx, promise);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		HeartbeatChannelHelper.setWriteTimestamp(ctx.channel());
		this.channellHandler.write(ctx, msg, promise);
	}

	@Override
	public ChannelInOutHandler getChannelHandler() {
		if(channellHandler instanceof ChannelHandlerDelegate)
		{
			return ((ChannelHandlerDelegate)channellHandler).getChannelHandler();
		}
		return channellHandler;
	}

	@Override
	public void close()
	{
		if(channellHandler != null)
		{
			this.channellHandler.close();
		}
	}
}
