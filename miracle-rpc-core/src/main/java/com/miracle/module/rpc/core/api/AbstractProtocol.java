package com.miracle.module.rpc.core.api;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.ConcurrentHashSet;
import com.miracle.module.rpc.common.utils.Constants;

public abstract class AbstractProtocol implements Protocol{
	
	private static Logger log = Logger.getLogger(AbstractProtocol.class);
	
	protected final Map<String/*service_key*/, Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>();
	
	protected final Set<Invoker<?>> invokers = new ConcurrentHashSet<Invoker<?>>();
	
	public static String serviceKey(RpcConfig config) {
		return serviceKey(config.getParameter(Constants.INTERFACE_KEY));
	}

	public static String serviceKey(String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(serviceName);
		return sb.toString();
	}
	
	@Override
	public void destroy() {
	    for (Invoker<?> invoker : invokers){
	        if (invoker != null) {
	            invokers.remove(invoker);
                try {
                    if (log.isInfoEnabled()) {
                    	log.info("Destroy reference: " + invoker.getConfig());
                    }
                    invoker.destroy();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
	    }
	    for (String key : new ArrayList<String>(exporterMap.keySet())) {
            Exporter<?> exporter = exporterMap.remove(key);
            if (exporter != null) {
                try {
                    if (log.isInfoEnabled()) {
                    	log.info("Unexport service: " + exporter.getInvoker().getConfig());
                    }
                    exporter.unexport();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
        }
	}
}
