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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;

public class NettyServer implements RpcServer {
	private static Logger log = Logger.getLogger(NettyServer.class);
	
	private final String host;
	private final int port;
	private final RpcConfig config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ServerBootstrap bootstrap;
	private ChannelFuture channelFuture;
	private final NettyHandler nettyHandler;
	
	public NettyServer(RpcConfig rpconfig, ChannelInOutHandler channelHandler)
	{
		config = rpconfig;
		this.nettyHandler = new NettyHandler(channelHandler);
        host = rpconfig.getParameter(Constants.HOST_KEY);
        port = rpconfig.getParameter(Constants.PORT_KEY, Constants.DEFAULT_PORT);
        
        try
        {
        	open();
        }
        catch(Throwable t)
        {
        	throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to bind netty server on " + port, t);
        }
        log.info("Create Netty server successfully on port " + port);
	}

	public String getServerHost()
	{
		return host;
	}
	public int getServerPort()
	{
		return port;
	}
	
	private void open() throws InterruptedException
	{
		int ioThreads = config.getParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS);
		int bossThreads = config.getParameter(Constants.BOSS_THREADS_KEY, Constants.DEFAULT_BOSS_THREADS);
		bossGroup = new NioEventLoopGroup(bossThreads, new NamedThreadFactory("ServerBoss", true));
		workerGroup = new NioEventLoopGroup(ioThreads, new NamedThreadFactory("ServerWorker", true));
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)
		.option(ChannelOption.SO_REUSEADDR, true)
		.option(ChannelOption.SO_KEEPALIVE, false)
		.childOption(ChannelOption.TCP_NODELAY, true)
		.childHandler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast("frameDecoder", new RpcPacketFrameDecoder());
				ch.pipeline().addLast("rpcRequestDecoder", new RpcDecoder());
				ch.pipeline().addLast("rpcResultEncoder", new RpcEncoder());
				ch.pipeline().addLast("rpcServerHandler", nettyHandler);
			}
			
		});
		channelFuture = bootstrap.bind(getServerPort()).sync();
	}
	
	public Collection<Channel> getChannels()
	{
		Collection<Channel> chs = new HashSet<Channel>();
		for(Channel ch : this.nettyHandler.getChannels().values())
		{
			if(ch.isActive())
			{
				chs.add(ch);
			}
		}
		return chs;
	}
	
	public void close()
	{
		if(channelFuture != null)
		{
			try
			{
				channelFuture.channel().close();
			}
			catch(Throwable t)
			{
				log.warn(t.getMessage(), t);
			}
			
			Collection<Channel> channels = getChannels();
			if(channels != null && channels.size() > 0)
			{
				for(Channel ch : channels)
				{
					try
					{
						ch.close();
					}
					catch(Exception e)
					{
						log.warn(e.getMessage(), e);
					}
				}
			}
			
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		if(this.nettyHandler != null)
		{
			this.nettyHandler.close();
		}
		log.info("Close netty server on port: " + port);
	}

	@Override
	public RpcConfig getConfig() {
		return this.config;
	}
}
