package com.miracle.module.rpc.core.api.wrapper.filter;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.miracle.module.rpc.core.api.*;
import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;


/**
 * 记录Service的Access Log。
 */
@Activate(group = Constants.PROVIDER_SIDE, providerorder = Integer.MIN_VALUE+200000)
public class AccessLogFilter implements Filter {
    
    private static final Logger log            = Logger.getLogger("kkrpc.accesslog");

    private static final String  MESSAGE_DATE_FORMAT   = "yyyy-MM-dd HH:mm:ss";

    @Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {
        try {
            RpcContext context = RpcContext.getContext();
            String serviceName = invoker.getInterface().getName();
            String version = invoker.getConfig().getParameter(Constants.VERSION_KEY);
            String group = invoker.getConfig().getParameter(Constants.GROUP_KEY);
            
            StringBuilder sn = new StringBuilder();
            sn.append("Request: [").append(new SimpleDateFormat(MESSAGE_DATE_FORMAT).format(new Date())).append("] ").append(context.getRemoteHost()).append(":").append(context.getRemotePort())
            .append(" -> ").append(invoker.getConfig().getIp()).append(":").append(invoker.getConfig().getPort())
            .append(" - ");
            sn.append("[").append(request.getId()).append("]");
            if (null != group && group.length() > 0) {
                sn.append(group).append("/");
            }
            sn.append(serviceName);
            if (null != version && version.length() > 0) {
                sn.append(":").append(version);
            }
            sn.append(" ");
            sn.append(request.getMethodName());
            
            Object[] args = request.getArguments();
            if (args != null && args.length > 0) {
                sn.append(" -- ");
            	int idx = 0;
            	for(Object arg : args)
            	{
            		sn.append(arg==null?"null":arg.toString());
            		if(idx++ != (args.length -1))
            		{
            			sn.append(", ");
            		}
            	}
            }
            log.info(sn.toString());
        } catch (Throwable t) {
            log.error("Exception in AccessLogFilter of service(" + invoker + " -> " + request + ")", t);
        }
        
        RpcResult result = invoker.invoke(request);
        logBizRpcResult(invoker, request, result);
        
        return result;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void logBizRpcResult(Invoker<?> invoker, RpcRequest request, RpcResult result)
    {
    	try
    	{
	    	RpcContext context = RpcContext.getContext();
	    	StringBuilder sn = new StringBuilder();
	    	String serviceName = invoker.getInterface().getName();
	    	String version = invoker.getConfig().getParameter(Constants.VERSION_KEY);
	          
	    	sn.append("Response: [").append(new SimpleDateFormat(MESSAGE_DATE_FORMAT).format(new Date())).append("] ")
	    	.append(invoker.getConfig().getIp()).append(":").append(invoker.getConfig().getPort())
	    	.append(" -> ").append(context.getRemoteHost()).append(":").append(context.getRemotePort())
	    	.append(" - ");
	    	sn.append("[").append(result.getId()).append("]");
	    	sn.append(serviceName);
	    	if (null != version && version.length() > 0) {
	    		sn.append(":").append(version);
	    	}
	    	sn.append(" ");
	    	sn.append(request.getMethodName());
	    	
	    	sn.append(" -- ");
	        
	    	if(result.hasException())
	    	{
	    		if(result.getException() != null)
	    		{
	    			sn.append("Got Exception: ").append(result.getException().toString());
	    		}
	    		else
	    		{
	    			sn.append("Got Exception: ").append(result.getProtoErr().toString());
	    		}
	    	}
	    	else 
	    	{
	    		if(result.getValue() == null)
	    		{
	    			sn.append("null");
	    		}
	    		else
	    		{
	    			Object retObj = result.getValue();
	    			if(retObj instanceof Collection)
	    			{
	    				Iterator it = ((Collection)retObj).iterator();
	    				sn.append("[");
	    				while(it.hasNext())
	    				{
	    					Object o = it.next();
	    					sn.append(o==null?"null":o.toString());
	    					if(it.hasNext())
	    					{
	    						sn.append(", ");
	    					}
	    				}
	    				sn.append("]");
	    			}
	    			else if(retObj instanceof Map)
	    			{
						Iterator<Map.Entry> iter = ((Map)retObj).entrySet().iterator();
						sn.append("{");
				        while (iter.hasNext()) {
				            Map.Entry entry = iter.next();
				            sn.append(entry.getKey()==null?"null":entry.getKey().toString());
				            sn.append('=');
				            sn.append(entry.getValue()==null?"null":entry.getValue().toString());
				            if (iter.hasNext()) {
				            	sn.append(", ");
				            }
				        }
				        sn.append("}");
	    			}
	    			else
	    			{
	    				sn.append(retObj.toString());
	    			}
	    		}
	    	}
	        
	        log.info(sn.toString());
    	} catch (Throwable t) {
            log.error("Exception in AccessLogFilter to output response", t);
        }
    }

}