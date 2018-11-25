package com.miracle.module.rpc.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RpcConfigTest {

	@Test
	public void testToJsonString() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("host", "10.0.0.12");
		map.put("port", "1234");
		map.put("sess.timeout", "20000");
		RpcConfig config = new RpcConfig(map);
		System.out.println(config.toJsonString());
	}

	@Test
	public void testParseFromJsonString() {
		String jsonString = "{\"port\":\"1234\",\"host\":\"10.0.0.12\",\"sess.timeout\":\"20000\"}";
		RpcConfig config = RpcConfig.parseFromJsonString(jsonString);
		System.out.println(config);
	}

}
