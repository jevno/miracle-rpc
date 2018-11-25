package com.miracle.module.rpc.config;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class MyTestConfig extends AbstractConfig{

	private String name;
	private Integer age;
	private Boolean isBoy;
	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Boolean getIsBoy() {
		return isBoy;
	}

	public void setIsBoy(Boolean isBoy) {
		this.isBoy = isBoy;
	}

	private static MyTestConfig configTest = new MyTestConfig();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("kkrpc.mytest.name", "ppmm");
		System.setProperty("kkrpc.mytest.is-boy", "false");
		appendProperties(configTest);
	}

	@Test
	public void testAppendProperties() {	
		assertEquals("ppmm", configTest.name);
		assertEquals((Integer)20, configTest.age);
		assertEquals(false, configTest.isBoy);
	}

	@Test
	public void testAppendParametersMapOfStringStringObjectString() {
		Map<String, String> kv = new HashMap<String, String>();
		appendParameters(kv, configTest);
		System.out.println(kv);
		assertTrue(kv.containsKey("name"));
		assertTrue(kv.containsKey("age"));
		assertTrue(kv.containsKey("is.boy"));
	}

	@Test
	public void testToString() {
		System.out.println(configTest.toString());
	}

}
