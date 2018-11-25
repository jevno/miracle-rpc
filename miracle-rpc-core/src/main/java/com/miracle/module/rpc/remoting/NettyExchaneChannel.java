package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

public class NettyExchaneChannel implements ExchangeChannel{
	private final Channel channel;
	private final RpcConfig config;
	
	public NettyExchaneChannel(Channel nettyChannel, RpcConfig config)
	{
		if(nettyChannel == null)
		{
			throw new IllegalArgumentException("channel == null");
		}
		channel = nettyChannel;
		this.config = config;
	}
	
	@Override
	public EventLoop eventLoop() {
		// TODO Auto-generated method stub
		return channel.eventLoop();
	}

	@Override
	public Channel parent() {
		// TODO Auto-generated method stub
		return channel.parent();
	}

	@Override
	public ChannelConfig config() {
		// TODO Auto-generated method stub
		return channel.config();
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return channel.isOpen();
	}

	@Override
	public boolean isRegistered() {
		// TODO Auto-generated method stub
		return channel.isRegistered();
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return channel.isActive();
	}

	@Override
	public ChannelMetadata metadata() {
		// TODO Auto-generated method stub
		return channel.metadata();
	}

	@Override
	public SocketAddress localAddress() {
		// TODO Auto-generated method stub
		return channel.localAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		// TODO Auto-generated method stub
		return channel.remoteAddress();
	}

	@Override
	public ChannelFuture closeFuture() {
		// TODO Auto-generated method stub
		return channel.closeFuture();
	}

	@Override
	public boolean isWritable() {
		// TODO Auto-generated method stub
		return channel.isWritable();
	}

	@Override
	public Unsafe unsafe() {
		// TODO Auto-generated method stub
		return channel.unsafe();
	}

	@Override
	public ChannelPipeline pipeline() {
		// TODO Auto-generated method stub
		return channel.pipeline();
	}

	@Override
	public ByteBufAllocator alloc() {
		// TODO Auto-generated method stub
		return channel.alloc();
	}

	@Override
	public ChannelPromise newPromise() {
		// TODO Auto-generated method stub
		return channel.newPromise();
	}

	@Override
	public ChannelProgressivePromise newProgressivePromise() {
		// TODO Auto-generated method stub
		return channel.newProgressivePromise();
	}

	@Override
	public ChannelFuture newSucceededFuture() {
		// TODO Auto-generated method stub
		return channel.newSucceededFuture();
	}

	@Override
	public ChannelFuture newFailedFuture(Throwable cause) {
		// TODO Auto-generated method stub
		return channel.newFailedFuture(cause);
	}

	@Override
	public ChannelPromise voidPromise() {
		// TODO Auto-generated method stub
		return channel.voidPromise();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress) {
		// TODO Auto-generated method stub
		return channel.bind(localAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress) {
		// TODO Auto-generated method stub
		return channel.connect(remoteAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			SocketAddress localAddress) {
		// TODO Auto-generated method stub
		return channel.connect(remoteAddress, localAddress);
	}

	@Override
	public ChannelFuture disconnect() {
		// TODO Auto-generated method stub
		return channel.disconnect();
	}

	@Override
	public ChannelFuture close() {
		// TODO Auto-generated method stub
		return channel.close();
	}

	@Override
	public ChannelFuture deregister() {
		// TODO Auto-generated method stub
		return channel.deregister();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.bind(localAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.connect(remoteAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public ChannelFuture disconnect(ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.disconnect(promise);
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.close(promise);
	}

	@Override
	public ChannelFuture deregister(ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.deregister(promise);
	}

	@Override
	public Channel read() {
		// TODO Auto-generated method stub
		return channel.read();
	}

	@Override
	public ChannelFuture write(Object msg) {
		// TODO Auto-generated method stub
		return channel.write(msg);
	}

	@Override
	public ChannelFuture write(Object msg, ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.write(msg, promise);
	}

	@Override
	public Channel flush() {
		// TODO Auto-generated method stub
		return channel.flush();
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
		// TODO Auto-generated method stub
		return channel.writeAndFlush(msg, promise);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg) {
		// TODO Auto-generated method stub
		return channel.writeAndFlush(msg);
	}

	@Override
	public <T> Attribute<T> attr(AttributeKey<T> key) {
		// TODO Auto-generated method stub
		return channel.attr(key);
	}

	@Override
	public int compareTo(Channel o) {
		// TODO Auto-generated method stub
		return channel.compareTo(o);
	}

	@Override
	public ResponseFuture request(Object request) throws RpcException {
		// TODO Auto-generated method stub
		
		return request(request, this.getConfig().getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
	}

	@Override
	public ResponseFuture request(Object request, int timeout)
			throws RpcException {
		// TODO Auto-generated method stub
		if(!channel.isActive())
		{
			throw new RpcException(RpcException.NETWORK_EXCEPTION, 
					"Failed to send request, cause this channel " + this + " is closed.");
		}
		DefaultFuture future = new DefaultFuture(this, (RpcRequest)request, timeout);
		try
		{
			this.writeAndFlush(request);
		}
		catch(Throwable e)
		{
			future.cancel();
			throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to send request to " + this, e);
		}
		return future;
	}

	@Override
	public RpcConfig getConfig() {
		// TODO Auto-generated method stub
		return this.config;
	}

	@Override
	public Channel getInnerChannel() {
		// TODO Auto-generated method stub
		return this.channel;
	}

}
