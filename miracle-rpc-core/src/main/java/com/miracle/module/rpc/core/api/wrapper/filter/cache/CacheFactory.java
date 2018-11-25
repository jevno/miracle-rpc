package com.miracle.module.rpc.core.api.wrapper.filter.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcRequest;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class CacheFactory {

	private final ConcurrentMap<String, Cache<BigInteger, Object>> caches =
			new ConcurrentHashMap<>();
    private Cache<BigInteger, Object> createCache(RpcConfig config, RpcRequest request) {
    	String methodName = request.getMethodName();
        final int max = config.getMethodParameter(methodName, Constants.CACHE_SIZE_KEY,
        		Constants.DEFAULT_CACHE_SIZE);
        int ttl = config.getMethodParameter(methodName, Constants.CACHE_TTL_KEY, Constants.DEFAULT_CACHE_TTL);
        int ttlInMilli = ttl * 1000;
        int initialCapacity = (int) ((float) max / 0.75F + 1.0F);

        Cache<BigInteger, Object> newCache = CacheBuilder.newBuilder()
        		.expireAfterWrite(ttlInMilli, TimeUnit.MILLISECONDS)
        		.initialCapacity(initialCapacity)
        		.maximumSize(max)
        		.build();
        return newCache;
    }
    
    public Cache<BigInteger, Object> getCache(RpcConfig config, RpcRequest request) {
        String key = request.getInterfaceName() +"."+ request.getMethodName();
        Cache<BigInteger, Object> cache = caches.get(key);
        if (cache == null) {
            caches.put(key, createCache(config, request));
            cache = caches.get(key);
        }
        return cache;
    }

}
