package com.miracle.module.rpc.core.api.wrapper.filter.tps;

import java.util.concurrent.atomic.AtomicInteger;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.RpcRequest;

public class TpsStat {
	private String name;
	private long lastResetTime;
	private long interval;
	private int rate;
	private AtomicInteger token;
	
	public TpsStat(String name, int rate, long interval)
	{
		this.name = name;
		this.rate = rate;
		this.interval = interval;
		this.lastResetTime = System.currentTimeMillis();
		this.token = new AtomicInteger(rate);
	}
	
	public long getLastResetTime()
	{
		return this.lastResetTime;
	}
	
	public int getToken()
	{
		return this.token.get();
	}
	
	@Override
	public String toString() {
        return new StringBuilder(32).append("StatItem ")
            .append("[name=").append(name).append(", ")
            .append("rate = ").append(rate).append(", ")
            .append("interval = ").append(interval).append("]")
            .toString();
    }
	
	public boolean isAllowable(RpcConfig url, RpcRequest invocation)
	{
		long now = System.currentTimeMillis();
		if(now > lastResetTime + interval)
		{
			token.set(rate);
			lastResetTime = now;
		}
		int value = token.get();
		boolean flag = false;
		while(value > 0 && !flag)
		{
			flag = token.compareAndSet(value, value-1);
			value = token.get();
		}
		return flag;
	}
}
