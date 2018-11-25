package com.miracle.module.rpc.core.api.wrapper.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.miracle.module.rpc.core.api.*;
import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.ExtensionLoader;
import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;


@Activate(group = {Constants.CONSUMER_SIDE, Constants.PROVIDER_SIDE}, auto = "true",
	providerorder = Integer.MIN_VALUE+50, consumerorder = Integer.MAX_VALUE-500000)
public class ContextFilter implements Filter {

	private static Logger log = Logger.getLogger(ContextFilter.class);
	
	private final List<RpcContextSwapIntercept> intercepters = new ArrayList<RpcContextSwapIntercept>();
	
	public ContextFilter()
	{
		Map<String, Class<?>>  interceptClssMap = ExtensionLoader.getExtensionClss(RpcContextSwapIntercept.class);
		if(interceptClssMap != null && interceptClssMap.size() > 0)
		{
			String[] names = new String[interceptClssMap.size()];
			names = interceptClssMap.keySet().toArray(names);
			List<Object> interceptInstances = 
					ExtensionLoader.getExtensionInstances(RpcContextSwapIntercept.class, names);
			if(interceptInstances != null && interceptInstances.size() > 0)
			{
				for(Object o : interceptInstances)
				{
					intercepters.add((RpcContextSwapIntercept) o);
				}
			}
		}
	}
	
	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
		
		boolean isProvider = invoker.getConfig().isProvider();
		
		if(isProvider)
		{
			Map<String, Object> attachment = request.getAttachments();
			if(attachment != null && attachment.size() > 0)
			{
				for(Map.Entry<String, Object> entry : attachment.entrySet())
				{
					RpcContext.getContext().setAttachment(entry.getKey(), (String)(entry.getValue()));
				}
			}
		}
		
		if(intercepters.size() > 0)
		{
			for(RpcContextSwapIntercept intercept : intercepters)
			{
				try{
					if(isProvider)
					{
						intercept.swapOutAsProvider();
					}
					else
					{
						intercept.swapInAsConsumer();
					}
				}
				catch(Throwable t)
				{
					log.error("Unexpected exception when handler context intercepter: " + intercept.getName(), t);
				}
			}
		}
		
		try {
            return invoker.invoke(request);
        } finally {
			if(intercepters.size() > 0)
			{
				for(RpcContextSwapIntercept intercept : intercepters)
				{
					try{
						if(isProvider)
						{
							intercept.swapDoneAsProvider();
						}
						else
						{
							intercept.swapDoneAsConsumer();
						}
					}
					catch(Throwable t)
					{
						log.error("Unexpected exception when handler context intercepter done: " + intercept.getName(), t);
					}
				}
			}
        	if(!isProvider)
        	{
        		RpcContext.getContext().clearAttachments();
        	}
        	else
        	{
        		RpcContext.removeContext();
        	}
        }
	}

}
