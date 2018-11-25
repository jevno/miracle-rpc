package com.miracle.module.rpc.core.api.wrapper.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.cache.Cache;
import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;

import com.miracle.module.rpc.core.api.*;
import com.miracle.module.rpc.core.api.wrapper.filter.cache.CacheFactory;
import com.miracle.module.rpc.core.api.wrapper.filter.metric.RpcMetricCollector;

@Activate(group = {Constants.CONSUMER_SIDE, Constants.PROVIDER_SIDE}, 
	providerorder = Integer.MIN_VALUE+10000, consumerorder = Integer.MAX_VALUE-10000)
public class CacheFilter implements Filter {

	 private final CacheFactory cacheFactory = new CacheFactory();

	 @Override
	 public RpcResult invoke(Invoker<?> invoker, RpcRequest request) throws RpcException {
		 
		String cacheKey = invoker.getConfig().getMethodParameter(request.getMethodName(), Constants.CACHE_KEY);
        if (cacheFactory != null && StringUtils.isNotEmpty(cacheKey) &&
        		cacheKey.equalsIgnoreCase("true"))
        {
        	Cache<BigInteger, Object> cache = cacheFactory.getCache(invoker.getConfig(), request);
            if (cache != null) {
            	BigInteger key = checksum(request.getArguments());
                if (cache != null) {
                    Object value = cache.getIfPresent(key);
                    if (value != null) {
                    	boolean isProviderSide = invoker.getConfig().isProvider();
                    	RpcMetricCollector.getInstance().markResponseFromCache(request, isProviderSide);
                        return new RpcResult(request.getId(), request.getSerializeType(), value);
                    }
                    RpcResult result = invoker.invoke(request);
                    if (!result.hasException()) {
                        cache.put(key, result.getValue());
                    }
                    return result;
                }
            }
        }
        return invoker.invoke(request);
    }
	 
	 public static BigInteger checksum(Object obj) 
	 {

		if (obj == null) {
		  return BigInteger.ZERO;   
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(oos != null)
			{
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		  
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(baos.toByteArray());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return new BigInteger(1, m.digest());
	 }

}
