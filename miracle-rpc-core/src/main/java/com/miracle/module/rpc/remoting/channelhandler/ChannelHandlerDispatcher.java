package com.miracle.module.rpc.remoting.channelhandler;

import com.miracle.module.rpc.core.api.ExecutionException;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.impl.DefaultProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

public class ChannelHandlerDispatcher extends WrappedChannelHandler {
	private final static Logger log = Logger.getLogger(ChannelHandlerDispatcher.class);
	
	public ChannelHandlerDispatcher(ChannelInOutHandler handler,
			DefaultProtocol protocol) {
		super(handler, protocol);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		log.debug("Dispatcher msg: " + msg + " from " + ctx.channel());

		if(msg instanceof RpcRequest)
		{
			RpcRequest request = (RpcRequest)msg;
			request.addAttachment("DispatchTime", String.valueOf(System.currentTimeMillis()));
		}

		ExecutorService cexecutor = getExecutor(msg); 
		try
		{
			cexecutor.execute(new ChannelEventRunnable(this.channellHandler, ctx, msg));
		}
		catch(RejectedExecutionException e)
		{
			log.error("Request is rejected by exector pool, req: " + msg, e);
			throw new ExecutionException(msg, "Request rejected by worker executor pool.");
		}
		catch(Throwable t)
		{
			log.error("Unexpected exception when process channelRead event of " + ctx.channel(), t);
		}
	}
}
