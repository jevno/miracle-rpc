package com.miracle.module.rpc.core.api.wrapper.filter;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.*;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Activate(group = {Constants.CONSUMER_SIDE, Constants.PROVIDER_SIDE}, auto = "true", 
	providerorder = Integer.MIN_VALUE+300000, consumerorder = Integer.MAX_VALUE-100)
public class CatTraceFilter implements Filter {

	 private static final String CLIENT_APPNAME_KEY = "ClientApp";

	 private static final Logger log            = Logger.getLogger("com.melot.kkrpc.cattrace");
	
	 private final ThreadLocal<Cat.Context> catCtx = 
    		new ThreadLocal<Cat.Context>() {
		    	 @Override 
		    	 protected Cat.Context initialValue() {    
		             return new Cat.Context() {
						
		            	public Map<String, String> maps = new HashMap<String, String>();
						@Override
						public String getProperty(String key) {
							return maps.get(key);
						}
						
						@Override
						public void addProperty(String key, String value) {
							maps.put(key, value);
						}
					};
		         }    
    		};
	@SuppressWarnings("serial")
	private static final Map<Integer, String> delayReportKeyMap = new HashMap<Integer , String>(){{
		 put(0, "[none]ms");
		 put(1, "[1-10)ms");
		 put(2, "[10-100)ms");
		 put(3, "[100-1000)ms");
		 put(4, "[1000-10000)ms");
		 put(5, "[10000-more)ms");
	 }};

	public static String getRequestDelayInQueue(long delayInMs)
	{
		int offset = 0;
		if(delayInMs <= 2)
		{
			return delayReportKeyMap.get(0);
		}
		while(delayInMs > 0 && offset < 5)
		{
			offset++;
			delayInMs /= 10;
		}
		return delayReportKeyMap.get(offset);
	}

	@Override
	public RpcResult invoke(Invoker<?> invoker, RpcRequest request)
			throws RpcException {

		Cat.Context ctx = catCtx.get();
		boolean isProviderSide = invoker.getConfig().isProvider();
		String serviceKey = invoker.getConfig().getServiceKey();
		String serviceName = serviceKey + "_" + request.getMethodName();
		String remoteAddress;
		String appName;

		Transaction t;
		if(isProviderSide)
		{
			ctx.addProperty(Cat.Context.ROOT, RpcContext.getContext().getAttachment(Cat.Context.ROOT));
			ctx.addProperty(Cat.Context.PARENT, RpcContext.getContext().getAttachment(Cat.Context.PARENT));
			ctx.addProperty(Cat.Context.CHILD, RpcContext.getContext().getAttachment(Cat.Context.CHILD));
			if(request.getAttachment(CLIENT_APPNAME_KEY) != null)
			{
				ctx.addProperty(CLIENT_APPNAME_KEY, RpcContext.getContext().getAttachment(CLIENT_APPNAME_KEY));
			}
			remoteAddress = RpcContext.getContext().getRemoteHost() + ":" +  RpcContext.getContext().getRemotePort();
			appName = ctx.getProperty(CLIENT_APPNAME_KEY);
			if(StringUtils.isEmpty(appName))
			{
				appName = remoteAddress;
			}

			t = Cat.getProducer().newTransaction("Service", serviceName);
			Cat.logRemoteCallServer(ctx);
			//fix melot-log no valid msgId problem
			MessageTree curTree = Cat.getManager().getThreadLocalMessageTree();
			String curMessageId = curTree.getMessageId();
			if (curMessageId == null) {
				curMessageId = Cat.createMessageId();
				curTree.setMessageId(curMessageId);
			}

			if(request.getAttachment("DispatchTime") != null)
			{
				long dispatchTime = Long.valueOf(request.getAttachment("DispatchTime"));
				long delayInQueue = System.currentTimeMillis() - dispatchTime;
				String delayName = getRequestDelayInQueue(delayInQueue);
				Event delayEvent = Cat.newEvent("Service.delayInQueue", delayName);
				delayEvent.addData("delayInMs", delayInQueue);
				delayEvent.setStatus(Event.SUCCESS);
				delayEvent.complete();
			}
			Cat.logEvent("Service.client", remoteAddress);
			Cat.logEvent("Service.app", appName);
		}
		else
		{
			remoteAddress = invoker.getConfig().getAddress();
			appName = serviceKey;
			t = Cat.getProducer().newTransaction("Call", serviceName);
			Cat.logRemoteCallClient(ctx);
			Cat.logEvent("Call.server", remoteAddress);
			Cat.logEvent("Call.app", appName);

			RpcContext.getContext().setAttachment(Cat.Context.ROOT, ctx.getProperty(Cat.Context.ROOT));
			RpcContext.getContext().setAttachment(Cat.Context.PARENT, ctx.getProperty(Cat.Context.PARENT));
			RpcContext.getContext().setAttachment(Cat.Context.CHILD, ctx.getProperty(Cat.Context.CHILD));
			if(!StringUtils.isEmpty(invoker.getConfig().getParameter(Constants.APP_NAME_KEY)))
			{
				RpcContext.getContext().setAttachment(CLIENT_APPNAME_KEY, invoker.getConfig().getParameter(Constants.APP_NAME_KEY));
			}
		}

		RpcResult result = null;
		try{
			long beginTm = System.currentTimeMillis();
			result = invoker.invoke(request);
			if(log.isInfoEnabled()) {
				log.info(kkrpcRequestLogMessage(invoker.getConfig(), request, isProviderSide) +
						" ==> response- " + kkRpcResponseLogMessage(result) +
						", cost::::" + Long.toString(System.currentTimeMillis() - beginTm) + "ms");
			}

		}
		catch(RpcException e)
		{
			t.setStatus(e);
			log.error(kkrpcRequestLogMessage(invoker.getConfig(), request, isProviderSide) + " rpcException occurs.", e);
			throw e;
		}
		finally{
			if(result != null)
			{
				if(!result.hasException())
				{
					t.setStatus(Transaction.SUCCESS);
				}
				else
				{
					if(result.getException() != null)
					{
						t.setStatus(result.getException());
					}
					else if(result.getProtoErr() != null && result.getProtoErr().getException() != null)
					{
						t.setStatus(result.getProtoErr().getException());
					}
				}
			}
			t.complete();
		}
	
		
		return result;
	}

	private String kkrpcRequestLogMessage(RpcConfig rpcConfig, RpcRequest request, boolean isProviderSide)
	{
		String serviceName = rpcConfig.getParameter(Constants.INTERFACE_KEY);
		String version = rpcConfig.getParameter(Constants.VERSION_KEY);
		String group = rpcConfig.getParameter(Constants.GROUP_KEY);

		StringBuilder sn = new StringBuilder();
		try {
			if(isProviderSide) {
				sn.append("Kkrpc provider handle");
			} else {
				sn.append("Kkrpc consumer submit");
			}
			sn.append(" request- [").append(request.getId()).append("]");
			if (null != group && group.length() > 0) {
				sn.append(group).append("/");
			}
			sn.append(serviceName);
			if (null != version && version.length() > 0) {
				sn.append(":").append(version);
			}
			sn.append(" ");
			sn.append(request.getMethodName());

			Object[] arguments = request.getArguments();
			if (arguments != null && arguments.length > 0) {
				sn.append(":::: [");
				int idx = 0;
				for (Object arg : arguments) {
					sn.append(arg == null ? "null" : arg.toString());
					if (idx++ != (arguments.length - 1)) {
						sn.append(", ");
					}
				}
				sn.append("]");
			}
		}
		catch(Throwable t)
		{
			log.warn("kkrpcRequestLogMessage failed", t);
		}
		return sn.toString();
	}

	private String kkRpcResponseLogMessage(RpcResult result)
	{
		StringBuilder sn = new StringBuilder();
		try
		{
			sn.append("::::");
			if(result != null) {
				if (result.hasException()) {
					if (result.getException() != null) {
						sn.append("Got biz Exception: ").append(result.getException().toString());
					} else {
						sn.append("Got biz Exception: ").append(result.getProtoErr().toString());
					}
				} else {
					if (result.getValue() == null) {
						sn.append("null");
					} else {
						Object retObj = result.getValue();
						if (retObj instanceof Collection) {
							Iterator it = ((Collection) retObj).iterator();
							sn.append("[");
							while (it.hasNext()) {
								Object o = it.next();
								sn.append(o == null ? "null" : o.toString());
								if (it.hasNext()) {
									sn.append(",");
								}
							}
							sn.append("]");
						} else if (retObj instanceof Map) {
							Iterator<Map.Entry> iter = ((Map) retObj).entrySet().iterator();
							sn.append("{");
							while (iter.hasNext()) {
								Map.Entry entry = iter.next();
								sn.append(entry.getKey() == null ? "null" : entry.getKey().toString());
								sn.append('=');
								sn.append(entry.getValue() == null ? "null" : entry.getValue().toString());
								if (iter.hasNext()) {
									sn.append(",");
								}
							}
							sn.append("}");
						} else {
							sn.append(retObj.toString());
						}
					}
				}
			}
		} catch (Throwable t) {
			log.warn("kkRpcResponseLogMessage failed.", t);
		}
		return sn.toString();
	}
}
