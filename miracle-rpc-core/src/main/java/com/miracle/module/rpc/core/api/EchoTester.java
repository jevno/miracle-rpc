package com.miracle.module.rpc.core.api;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.remoting.NettyClient;
import com.miracle.module.rpc.remoting.channelhandler.ExchangeHandlerAdapter;
import com.miracle.module.rpc.remoting.channelhandler.HeaderExchangeHandler;

public class EchoTester {

	private final static Logger log = Logger.getLogger(EchoTester.class);
	private static NettyClient getNettyClient(String host, int port)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(Constants.HOST_KEY, host);
		params.put(Constants.PORT_KEY, String.valueOf(port));
		params.put(Constants.IO_THREADS_KEY, String.valueOf(1));
		RpcConfig config = new RpcConfig(params);
		NettyClient client = new NettyClient(config, new HeaderExchangeHandler(new ExchangeHandlerAdapter(){

			@Override
			public Object reply(Channel channel, Object request)
					throws RpcException {
				//do nothing here
				return null;
			}
			
		}));
		return client;
	}
	
	public static boolean echoTest(String host, int port)
	{
		NettyClient client = getNettyClient(host, port);
		if(client != null)
		{
			try{
				RpcRequest echoReq = new RpcRequest();
				echoReq.setInterfaceName("Echo");
				echoReq.setMethodName("echo");
				RpcResult result = (RpcResult) client.request(echoReq, Constants.DEFAULT_TIMEOUT).get();
				int val = (Integer) result.getValue();
				if(val == 0) {
					return true;
				}
			}
			catch(Throwable e)
			{
				log.warn(e.getMessage(), e);
			}
			client.close();
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		System.out.println(echoTest("10.0.3.108", 2345));
	}
}
