package com.miracle.module.rpc.jmeter;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcHeader;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import com.miracle.module.rpc.remoting.NettyClient;
import com.miracle.module.rpc.remoting.ResponseFuture;
import com.miracle.module.rpc.remoting.channelhandler.ChannelInOutHandler;
import com.miracle.module.rpc.remoting.channelhandler.ExchangeHandlerAdapter;
import com.miracle.module.rpc.remoting.channelhandler.HeaderExchangeHandler;
import com.miracle.module.rpc.remoting.heartbeat.HeartbeatChannelHandler;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


public class JMeterKkrpcClient {
	private static Logger log = Logger.getLogger(JMeterKkrpcClient.class);
	
	private static Map<String, JMeterKkrpcClient> gClientMap = 
			Collections.synchronizedMap(new HashMap<String, JMeterKkrpcClient>());
	
	private String host;
	private int port;
	private NettyClient rpcClient;
	private static AtomicInteger idCnt = new AtomicInteger(0);
	
	public static synchronized JMeterKkrpcClient getKkrpcClient(String host, int port)
	{
		String key = host + ":" + port;
		JMeterKkrpcClient client = gClientMap.get(key);
		if(client == null)
		{
			client = new JMeterKkrpcClient(host, port);
			gClientMap.put(key, client);
		}
		return client;
	}
	
	public static synchronized void destroyAll()
	{
		if(gClientMap != null && gClientMap.size() > 0)
		{
			for(Map.Entry<String, JMeterKkrpcClient>  entry : gClientMap.entrySet())
			{
				JMeterKkrpcClient client = entry.getValue();
				client.rpcClient.close();
			}
		}
	}
	
	private JMeterKkrpcClient(String host, int port)
	{
		this.host = host;
		this.port = port;
		
		initKkrpcClient();
	}
	
	private boolean initKkrpcClient()
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(Constants.HOST_KEY, this.host);
		params.put(Constants.PORT_KEY, String.valueOf(this.port));
		
		RpcConfig rpcConfig = new RpcConfig(params);
		ChannelInOutHandler channelHandler = new HeartbeatChannelHandler(
				new HeaderExchangeHandler(
						new ExchangeHandlerAdapter()
						{

							@Override
							public Object reply(Channel channel, Object request)
									throws RpcException {
								log.error("It should not reach here.");
								return null;
							}
							
						}));
		rpcClient = new NettyClient(rpcConfig, channelHandler);
		return true;
	}
	
	public String sendRequestSync(String interfaceName, String methodName, Object[] arguments)
	{
		RpcRequest req = new RpcRequest();
		req.setId(idCnt.addAndGet(1));
		req.setSerializeType(RpcHeader.CODEC_FASTJSON);
		req.setInterfaceName(interfaceName);
		req.setMethodName(methodName);
		req.setArguments(arguments);
		
		ResponseFuture future = rpcClient.request(req, 8000);
		RpcResult ret = (RpcResult) future.get();
		if(ret == null)
			return "null";
		else
			return ret.toString();
	}
}
