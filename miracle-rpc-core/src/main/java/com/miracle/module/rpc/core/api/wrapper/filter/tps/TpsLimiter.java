package com.miracle.module.rpc.core.api.wrapper.filter.tps;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcRequest;

public class TpsLimiter {
	 private final ConcurrentMap<String, TpsStat> stats = new ConcurrentHashMap<String, TpsStat>();

	 
	 private boolean isInterfaceAllowable(RpcConfig config, RpcRequest request)
	 {
		 int rate = config.getParameter(Constants.TPS_LIMIT_RATE_KEY, -1);
		 long interval = config.getParameter(Constants.TPS_LIMIT_INTERVAL_KEY,
                                      Constants.DEFAULT_TPS_LIMIT_INTERVAL);
	     String serviceKey = config.getServiceKey();
	     if (rate > 0) {
	    	 TpsStat statItem = stats.get(serviceKey);
	         if (statItem == null) {
	             stats.putIfAbsent(serviceKey,
	                               new TpsStat(serviceKey, rate, interval));
	             statItem = stats.get(serviceKey);
	         }
	         return statItem.isAllowable(config, request);
	     } else {
	    	 TpsStat statItem = stats.get(serviceKey);
	         if (statItem != null) {
	             stats.remove(serviceKey);
	         }
	     }
	     return true;
	 }
	 
	 private boolean isMethodAllowable(RpcConfig config, RpcRequest request)
	 {
		 String methodName = request.getMethodName();
		 int rate = config.getMethodParameter(methodName, Constants.TPS_LIMIT_RATE_KEY, -1);
		 long interval = config.getMethodParameter(methodName, Constants.TPS_LIMIT_INTERVAL_KEY,
                                      Constants.DEFAULT_TPS_LIMIT_INTERVAL);
	     String serviceKey = config.getServiceKey();
	     String statKey = serviceKey + "_" + methodName;
	     if (rate > 0) {
	    	 TpsStat statItem = stats.get(statKey);
	         if (statItem == null) {
	             stats.putIfAbsent(statKey,
	                               new TpsStat(statKey, rate, interval));
	             statItem = stats.get(statKey);
	         }
	         return statItem.isAllowable(config, request);
	     } else {
	    	 TpsStat statItem = stats.get(statKey);
	         if (statItem != null) {
	             stats.remove(statKey);
	         }
	     }
	     return true;
	 }
	 
	 public boolean isAllowable(RpcConfig config, RpcRequest request) {
		
		 if(isInterfaceAllowable(config, request))
		 {
			 return isMethodAllowable(config, request);
		 }
	
	     return false;
	 }
}
