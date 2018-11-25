package com.miracle.module.rpc.cluster.gray;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcRequest;
import org.apache.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class RandomRule implements HitRule{
	
	private static Logger log = Logger.getLogger(RandomRule.class);

	private int range = 10;
	
	public RandomRule(String arg)
	{
		try{
			range = Integer.valueOf(arg);
		}
		catch(Exception e)
		{
			log.error("Bad rule argument for random rule:" + arg, e);
		}
	}
	
	public static int nextInt(int minValue, int maxValue) {
		
		if(maxValue - minValue <= 0) {
			return minValue;
		}
		
		return minValue + ThreadLocalRandom.current().nextInt(maxValue - minValue + 1);
	}
	
	public static boolean isRandHit(int rate, int maxValue) {
		return nextInt(1, maxValue) <= rate;
	}
	
	@Override
	public <T> boolean isHit(Invoker<T> inv, RpcRequest req) {
		
		return isRandHit(1, range);
	}

}
