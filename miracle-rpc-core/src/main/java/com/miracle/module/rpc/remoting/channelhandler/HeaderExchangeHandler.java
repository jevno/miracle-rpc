package com.miracle.module.rpc.remoting.channelhandler;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;
import com.miracle.module.rpc.remoting.DefaultFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import org.apache.log4j.Logger;

public class HeaderExchangeHandler extends ChannelHandlerDelegate {

	private final static Logger log = Logger
			.getLogger(HeaderExchangeHandler.class);
	private final ExchangeHandler handler;

	public HeaderExchangeHandler(ExchangeHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}

		this.handler = handler;
	}

	private RpcResult handleRequest(Channel channel, RpcRequest request) {
		RpcResult response = null;
		try {
			response = (RpcResult) handler.reply(channel, request);
		} catch (RpcException e) {
			response = new RpcResult();
			response.setId(request.getId());
			response.setSerializeType(request.getSerializeType());
			response.setException(e);
		} catch (Throwable t) {
			response = new RpcResult();
			response.setId(request.getId());
			response.setSerializeType(request.getSerializeType());
			response.setException(new RpcException(
					RpcException.UNKNOWN_EXCEPTION,
					"Caught exception when processing RpcRequest.", t));
			log.error("Unknown exception while handleRequest.", t);
		}
		return response;
	}

	private void handleResponse(Channel channel, RpcResult response) {
		if (response != null) {
			DefaultFuture.received(channel, response);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof RpcRequest) {
			if (ctx.channel().isActive()) {
				RpcRequest request = (RpcRequest) msg;
				RpcResult result = handleRequest(ctx.channel(), request);
				ctx.channel().writeAndFlush(result);
			}
		} else if (msg instanceof RpcResult) {
			handleResponse(ctx.channel(), (RpcResult) msg);
		} else {
			log.error("Unknown rpc message: " + msg.getClass().getName()
					+ " from " + ctx.channel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.warn("Exception caught :" + ctx.channel(), cause);
		if (cause instanceof ExecutionException) {
			ExecutionException ex = (ExecutionException) cause;
			if (ex.getRequest() != null
					&& ex.getRequest() instanceof RpcRequest) {
				if (ctx.channel().isActive()) {
					RpcRequest rejectedRequest = (RpcRequest) ex.getRequest();
					RpcMetricCollector.getInstance()
							.markProviderThreadPoolRejected(rejectedRequest);
					RpcResult response = new RpcResult();
					response.setId(rejectedRequest.getId());
					response.setSerializeType(rejectedRequest
							.getSerializeType());
					response.setException(new RpcException(
							RpcException.EXECUTION_EXCEPTION, ex.getMessage(),
							cause));
					ctx.channel().writeAndFlush(response);
				}
			}
			return;
		} else if (cause instanceof EncoderException
				&& cause.getCause() != null
				&& cause.getCause() instanceof OutEncodeException) {
			OutEncodeException ex = (OutEncodeException) cause.getCause();
			if (ex.getRequest() != null) {
				// 请求还是响应,响应返回异常
				if (ex.getRequest() instanceof RpcRequest) {

					RpcRequest rpcRequest = (RpcRequest) ex.getRequest();
					RpcResult response = new RpcResult();
					response.setId(rpcRequest.getId());
					response.setException(new RpcException(
							RpcException.RESPONSE_PACKET_TOO_LONG_EXCEPTION, ex
									.getMessage()));
					DefaultFuture.received(ctx.channel(), response);

				} else if (ex.getRequest() instanceof RpcResult) {
					if (ctx.channel().isActive()) {
						RpcResult rpcResult = (RpcResult) ex.getRequest();
						RpcResult response = new RpcResult();
						response.setId(rpcResult.getId());
						response.setSerializeType(rpcResult.getSerializeType());
						response.setException(new RpcException(
								RpcException.RESPONSE_PACKET_TOO_LONG_EXCEPTION,
								ex.getMessage()));
						ctx.channel().writeAndFlush(response);
					}
				}
			}
		} else {
			// need close channel here???
			ctx.channel().close();
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("New connection: " + ctx.channel());
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("Close connection: " + ctx.channel());
		ctx.fireChannelInactive();
	}

	@Override
	public ChannelInOutHandler getChannelHandler() {
		if (handler instanceof ChannelHandlerDelegate) {
			return ((ChannelHandlerDelegate) handler).getChannelHandler();
		}
		return handler;
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("close: " + ctx.channel());
		}
		ctx.close(promise);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("write: " + msg);
		}
		ctx.write(msg, promise
				.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE));
	}

	@Override
	public void close() {
		if (this.handler != null) {
			this.handler.close();
		}
	}
}
