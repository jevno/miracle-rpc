package com.miracle.module.rpc.cluster.gray;

import java.util.HashMap;
import java.util.Map;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RandomRuleTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Constants.GRAY_RULE_KEY, "rand");
		params.put(Constants.GRAY_RULE_ARG_KEY, "2");
		RpcConfig config = new RpcConfig(params);
		RandomRule rule = (RandomRule) RuleFactory.getInstance().getRule(config);
		int cnt = 0;
		for(int i=0; i<10; i++)
		{
			if(rule.isHit(null, null))
			{
				cnt ++;
				System.out.println("hits");
			}
		}
		System.out.println("hit count: " + cnt);
	}

}
