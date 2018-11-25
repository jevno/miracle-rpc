package com.miracle.module.rpc.core.impl;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.remoting.NettyClient;
import com.miracle.module.rpc.remoting.NettyServer;
import com.miracle.module.rpc.remoting.RpcClient;
import com.miracle.module.rpc.remoting.RpcServer;
import com.miracle.module.rpc.remoting.channelhandler.ChannelHandlerDispatcher;
import com.miracle.module.rpc.remoting.channelhandler.ExchangeHandler;
import com.miracle.module.rpc.remoting.channelhandler.ExchangeHandlerAdapter;
import com.miracle.module.rpc.remoting.channelhandler.HeaderExchangeHandler;
import com.miracle.module.rpc.remoting.heartbeat.HeartbeatChannelHandler;
import com.miracle.module.rpc.remoting.heartbeat.HeartbeatClient;
import com.miracle.module.rpc.remoting.heartbeat.HeartbeatServer;
import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.ProtocolFilterWrapper;
import com.miracle.module.rpc.core.api.wrapper.ProtocolListenerWrapper;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultProtocol extends AbstractProtocol {

	private static Logger log = Logger.getLogger(DefaultProtocol.class);
	private static Protocol INSTANCE = new ProtocolListenerWrapper(new ProtocolFilterWrapper(new DefaultProtocol()));
	
	private final Map<String/*ip:port*/, RpcServer> serverMap = new ConcurrentHashMap<String, RpcServer>();
	
	private ExchangeHandler requestHandler = new ExchangeHandlerAdapter(){

		@Override
		public Object reply(Channel channel, Object request)
				throws RpcException {
			if(request instanceof RpcRequest)
			{
				RpcContext.getContext().setRemoteAddress((InetSocketAddress) channel.remoteAddress());
				
				Invoker<?> invoker = getInvoker(channel, (RpcRequest)request);	
				return invoker.invoke((RpcRequest)request);
			}
			else
			{
				log.error("Unsupported request:" + request.getClass().getName());
				throw new RpcException(RpcException.UNSUPPORT_EXCEPTION, "Unsupported request:" + request.getClass().getName());
			}
		}

	};
	
	Invoker<?> getInvoker(Channel channel, RpcRequest request)
	{
		//handle echo test
		if(Constants.ECHO_INTERFACE.equals(request.getInterfaceName()))
		{
			return this.exporterMap.values().iterator().next().getInvoker();
		}
		
		String serviceKey = serviceKey(request.getInterfaceName());
		DefaultExporter<?> exporter = (DefaultExporter<?>) this.exporterMap.get(serviceKey);
		if(exporter == null)
		{
			String errmsg = "Not found exported service: " + serviceKey
					+ "in " + exporterMap.keySet() + ", channel: client:" + channel.remoteAddress() 
					+ " ---> server:" + channel.localAddress() + ", request: " + request;
			
			log.error(errmsg);
			throw new RpcException(RpcException.NOINVOKER_EXCEPTION, errmsg);
		}
		return exporter.getInvoker();
	}
	
	public static Protocol getInstance()
	{
		return INSTANCE;
	}
	
	public RpcConfig getExporterConfigByInfName(String interfaceName)
	{
		if(!StringUtils.isEmpty(interfaceName))
		{
			Exporter<?> exporter = this.exporterMap.get(interfaceName);
			if(exporter != null)
			{
				return exporter.getInvoker().getConfig();
			}
		}
		if(this.exporterMap.size() > 0)
		{
			for(Exporter<?> exporter : this.exporterMap.values())
			{
				return exporter.getInvoker().getConfig();
			}
		}
		return null;
	}
	
	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		String key = serviceKey(invoker.getConfig());
		DefaultExporter<T>  exporter = new DefaultExporter<T>(invoker, key, this.exporterMap);
		this.exporterMap.put(key, exporter);
		
		openServer(invoker.getConfig());
		
		return exporter;
	}
	
	private void openServer(RpcConfig config)
	{
		String address = config.getAddress();
		RpcServer server = serverMap.get(address);
		if(server == null)
		{
			serverMap.put(address, createServer(config));
		}
	}
	
	private RpcServer createServer(RpcConfig config)
	{
		return new HeartbeatServer(
				new NettyServer(config, 
						new HeartbeatChannelHandler(
								new ChannelHandlerDispatcher(
										new HeaderExchangeHandler(requestHandler), this))));

	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, RpcConfig config)
			throws RpcException {
		config = config.addParameter(Constants.CHECK_CONNECTION, false);
		DefaultInvoker<T> invoker = new DefaultInvoker<T>(type, config, getClients(config), this.invokers);
		this.invokers.add(invoker);
		return invoker;
	}

	private RpcClient[] getClients(RpcConfig config)
	{
		int connections = config.getParameter(Constants.CONNECTIONS_KEY, Constants.DEFAULT_CONNECTIONS);
		RpcClient[] clients = new RpcClient[connections];
		for(int i=0; i<clients.length; i++)
		{
			clients[i] = initClient(config);
		}
		return clients;
	}
	
	private RpcClient initClient(RpcConfig config)
	{
		return new HeartbeatClient(new NettyClient(config, 
				new HeartbeatChannelHandler(new HeaderExchangeHandler(requestHandler))));
	}
	
	@Override
    public void destroy()
	{
		for(String key : new ArrayList<String>(serverMap.keySet()))
		{
			RpcServer server = serverMap.remove(key);
			if(server != null)
			{
				server.close();
			}
		}
		super.destroy();
	}
}
