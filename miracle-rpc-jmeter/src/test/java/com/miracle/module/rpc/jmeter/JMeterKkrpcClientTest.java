package com.miracle.module.rpc.jmeter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JMeterKkrpcClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		JMeterKkrpcClient client = JMeterKkrpcClient.getKkrpcClient("10.0.1.214", 2345);
		if(client != null)
		{
			Object[] arguments = new Object[1];
			arguments[0] = 686456600;
			String ret = client.sendRequestSync("com.melot.kkcore.user.service.KkUserService", "getUserDetailInfo", arguments);
			System.out.println(ret);
		}
	}

}
