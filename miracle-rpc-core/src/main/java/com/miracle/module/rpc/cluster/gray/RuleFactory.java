package com.miracle.module.rpc.cluster.gray;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class RuleFactory {
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Map<String, HitRule> RULES = new ConcurrentHashMap<String, HitRule>();
	
	private static class FactoryHolder
	{
		public static final RuleFactory FACTORY = new RuleFactory();
	}
	
	public static RuleFactory getInstance()
	{
		return FactoryHolder.FACTORY;
	}
	
	public HitRule getRule(RpcConfig config)
	{
		String ruleKey = config.getParameter(Constants.GRAY_RULE_KEY, Constants.DEFAULT_GRAY_RULE);
		String ruleArg = config.getParameter(Constants.GRAY_RULE_ARG_KEY, Constants.DEFAULT_GRAY_RULE_ARG);
		
		HitRule rule = RULES.get(ruleKey);
		if(rule == null)
		{
			LOCK.lock();
			try
			{	
				rule = createRule(ruleKey, ruleArg);
				RULES.put(ruleKey, rule);
			}
			finally
			{
				LOCK.unlock();
			}
		}
		return rule;
	}
	
	private HitRule createRule(String name, String ruleArg)
	{
		if(name.equals(Constants.FAILOVER_CLUSTER))
			return new RandomRule(ruleArg);
		
		return new RandomRule(ruleArg);
	}
}
