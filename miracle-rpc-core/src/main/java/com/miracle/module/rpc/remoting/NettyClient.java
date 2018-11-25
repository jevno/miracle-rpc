package com.miracle.module.rpc.remoting;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;
import com.miracle.module.rpc.remoting.channelhandler.ChannelInOutHandler;
import com.miracle.module.rpc.remoting.channelhandler.NettyHandler;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.codec.RpcDecoder;
import com.miracle.module.rpc.core.codec.RpcEncoder;
import com.miracle.module.rpc.core.codec.RpcPacketFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NettyClient implements RpcClient{
	private static Logger log = Logger.getLogger(NettyClient.class);
	private volatile ExchangeChannel channel;
	private int timeout;
	private int connectTimeout;
	private String remoteHost;
	private int remotePort;
	private RpcConfig config;
	private Bootstrap bootstrap;
	private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS, new NamedThreadFactory("NettyClientWorker", true));
	private final NettyHandler nettyHandler;
	private volatile boolean     closed = false;
	
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("NettyClientReconnectTimer", true));
    private volatile ScheduledFuture<?>  reconnectExecutorFuture = null;
    private final Lock connectLock = new ReentrantLock();
    private final AtomicInteger reconnectCount = new AtomicInteger(0);
	
	public int getTimeout() {
		return timeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}
	
	public ExchangeChannel getChannel()
	{
		return channel;
	}
	
	public ResponseFuture request(Object req, int timeout)
	{
		if(this.channel != null && !this.channel.isActive())
		{
			connect();
		}
		if(this.channel != null && this.channel.isActive())
		{
			return this.channel.request(req, timeout);
		}
		else
		{
			throw new RpcException(RpcException.NETWORK_EXCEPTION,
					"No valid connection to [" +remoteHost+":" + remotePort + "]");
		}
	}
	
	public NettyClient(RpcConfig config, ChannelInOutHandler channelHandler)
	{
		this.config = config;
		this.nettyHandler = new NettyHandler(channelHandler);
		this.timeout = config.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
		this.connectTimeout = config.getParameter(Constants.CONNECT_TIMEOUT_KEY, Constants.DEFAULT_CONNECT_TIMEOUT);	
		this.remoteHost = config.getParameter(Constants.HOST_KEY);
		this.remotePort = config.getParameter(Constants.PORT_KEY, Constants.DEFAULT_PORT);
		
		try{
			open();
		}
		catch(RpcException e)
		{
			close();
			throw e;
		}
		catch(Throwable t)
		{
			close();
			throw new RpcException(RpcException.NETPROTOCOL_EXCEPTION, "Create netty client open failed", t);
		}
		
		try{
			connect();
		}
		catch(RpcException e)
		{
			if(config.getParameter(Constants.CHECK_CONNECTION, true))
			{
				close();
				throw e;
			}
			else
			{
				log.warn("Failed to connect to the server " + this.remoteHost + ":" + this.remotePort + " (check == false, ignore and retry later!), cause: " + e.getMessage(), e);
			}
		}
		catch(Throwable t)
		{
			close();
			throw new RpcException(RpcException.NETPROTOCOL_EXCEPTION, "Create netty client connect failed", t);
		}
		
		log.info("Create netty client successfully to " + this.remoteHost + ":" + this.remotePort);
	}
	
	private void open()
	{
		bootstrap = new Bootstrap();
		bootstrap.group(nioEventLoopGroup)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.SO_KEEPALIVE, false)
//		.option(ChannelOption.SO_TIMEOUT, getTimeout())
		.handler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast("frameDecoder", new RpcPacketFrameDecoder());
				ch.pipeline().addLast("rpcResultDecoder", new RpcDecoder());
				ch.pipeline().addLast("rpcRequestEncoder", new RpcEncoder());
				ch.pipeline().addLast("rpcClientHandler", nettyHandler);
			}
			
		});
	}
	
	private void connect()
	{
		connectLock.lock();
		try {
			if(isConnected())
				return;
			
			initConnectStatusCheckTask();
			
			ChannelFuture future = bootstrap.connect(getConnectAddress());
			
			try
			{
				boolean bRet = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);
				if(bRet && future.isSuccess())
				{
					Channel newChannel = future.channel();
					try
					{
						Channel oldChannel = this.channel;
						if(oldChannel != null)
						{
							oldChannel.close();
						}
					}
					finally
					{
						if(this.isClosed())
						{
							try{
								newChannel.close();
							}
							finally
							{
								this.channel = null;
							}
						}
						else
						{
							this.channel = new NettyExchaneChannel(newChannel, config);
						}
					}
				}
				else if(future.cause() != null)
				{
					throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to connect server " + getRemoteAddress(), future.cause());
				}
				else
					throw new RpcException(RpcException.NETWORK_EXCEPTION, "Timeout when connect to server " + getRemoteAddress());
			}
			finally
			{
				if(!isConnected())
				{
					future.cancel(true);
				}
			}
			
		} 
		finally
		{
			connectLock.unlock();
		}
		
	}
	
	public void disconnect()
	{
		connectLock.lock();
		try
		{
			this.destroyConnectStatusCheckTask();
			try
			{
				if(channel != null)
				{
					channel.close();
				}
			}
			catch(Throwable t)
			{
				log.error(t.getMessage(), t);
			}
		}
		finally
		{
			connectLock.unlock();
		}
	}
	
	public Collection<Channel> getChannels()
	{
		if(channel != null && channel.isActive())
		{
			return Collections.singletonList(channel.getInnerChannel());
		}
		return null;
	}
	
	public SocketAddress getConnectAddress() {
        return new InetSocketAddress(remoteHost, remotePort);
    }

    public SocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return getConnectAddress();
        return channel.remoteAddress();
    }

    public SocketAddress getLocalAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return InetSocketAddress.createUnresolved("127.0.0.1", 0);
        return channel.localAddress();
    } 
	
	public boolean isConnected()
	{
		if(channel == null)
		{
			return false;
		}
		return channel.isActive();
	}
	
	public boolean isClosed()
	{
		return this.closed;
	}
	
	public void close()
	{
		if(this.closed)
			return;
		
		disconnect();
		if(this.nettyHandler != null)
		{
			this.nettyHandler.close();
		}
		this.closed = true;
		log.info("Close Netty client to " + this.remoteHost + ":" + this.remotePort);
	}
	
	private synchronized void initConnectStatusCheckTask()
	{
		int reconnectPeriod = this.config.getParameter(Constants.RECONNECT_KEY, Constants.DEFAULT_RECONNECT_PERIOD);
		if(reconnectPeriod > 0 && (reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled()))
		{
			Runnable reconnectTask = new Runnable() {

				@Override
				public void run() {
					try {
						if(!isConnected())
						{
							connect();
						}
					}
					catch(Throwable t)
					{
						log.warn("Netty client connnect to server " + remoteHost+":"+remotePort +
								"failed #" + reconnectCount.incrementAndGet(), t);
					}
				}
				
			};
			reconnectExecutorFuture = reconnectExecutorService.scheduleAtFixedRate(reconnectTask, 
					reconnectPeriod, reconnectPeriod, TimeUnit.MILLISECONDS);
		}
	}
	
	private synchronized void destroyConnectStatusCheckTask()
	{
		try {
			if(reconnectExecutorFuture != null && !reconnectExecutorFuture.isDone())
			{
				reconnectExecutorFuture.cancel(true);
				reconnectExecutorService.purge();
			}
		}
		catch(Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	@Override
	public RpcConfig getConfig() {
		return this.config;
	}
}
