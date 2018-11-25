package com.miracle.module.rpc.core.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class RpcRequestTest {

	@Test
	public void test() {
		RpcObject obj = new RpcRequest();
		System.out.println(obj.getClass());
		assertEquals(obj.getClass().getSimpleName(), "RpcRequest");
	}

}
