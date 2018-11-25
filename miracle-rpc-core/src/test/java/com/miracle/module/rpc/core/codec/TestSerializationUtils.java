package com.miracle.module.rpc.core.codec;


import java.util.ArrayList;
import java.util.List;

import com.miracle.module.rpc.core.api.RpcRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestSerializationUtils {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		RpcRequest req = new RpcRequest();
		req.setId(1);
		req.setSerializeType((short) 2);
		req.setInterfaceName("org.kkrpc.example.Calculator");
		req.setMethodName("add");
		Object[] params = new Object[2];
		params[0] = new Integer(10);
		params[1] = new Integer(20);
		req.setArguments(params);
		
		byte[] buf = SerializationUtils.serializeFastjson(req);
		System.out.println(buf.length);
	
		System.out.println(new String(buf));
	}
	
	@Test
	public void testListParams() {
		RpcRequest req = new RpcRequest();
		req.setId(1);
		req.setSerializeType((short) 2);
		req.setInterfaceName("org.kkrpc.example.Calculator");
		req.setMethodName("getCombinedListSize");
		Object[] params = new Object[2];
		List<String> list1 = new ArrayList<String>();
		list1.add("aaa");
		params[0] = list1;
		List<String> list2 = new ArrayList<String>();
		list2.add("aaa");
		list2.add("bbb");
		params[1] = list2;
		req.setArguments(params);
		
		byte[] buf = SerializationUtils.serializeFastjson(req);
		System.out.println(buf.length);
	
		System.out.println(new String(buf));
	}
	
	@Test 
	public void testHeartbeat() {
		RpcRequest req = new RpcRequest();
		req.setId(1);
		req.setSerializeType((short) 2);
		req.setInterfaceName("HB");
		req.setMethodName("HB_ECHO");
		byte[] buf = SerializationUtils.serializeFastjson(req);
		System.out.println(buf.length);
	
		System.out.println(new String(buf));
	}
}
