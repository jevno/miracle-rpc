package com.miracle.module.rpc.core.api.wrapper.filter;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CacheFilterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChecksum() {
		assertEquals(BigInteger.ZERO, CacheFilter.checksum(null));
		assertNotEquals(CacheFilter.checksum(new Object[]{new Integer(10)}),
				CacheFilter.checksum(new Object[]{new Integer(100)}));
		assertNotEquals(CacheFilter.checksum(new Object[]{new Integer(10), new String("10")}),
				CacheFilter.checksum(new Object[]{new String("10"), new Integer(10)}));
		
		assertEquals(CacheFilter.checksum(new Object[]{new Integer(10), new Integer(100)}),
				CacheFilter.checksum(new Object[]{new Integer(10), new Integer(100)}));
		
		Map<String, List<Integer>> complexParam = new HashMap<String, List<Integer>>();
		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(10);
		list1.add(5690);
		list1.add(223565);
		list1.add(789498);
		list1.add(1981565);
		complexParam.put("asdfasdfas", list1);
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(10);
		list2.add(5690);
		list2.add(223565);
		list2.add(789498);
		list2.add(1981565);
		complexParam.put("asdfasdfasdfasdfasdfasdf", list2);
		complexParam.put("asdf", list2);
		complexParam.put("asdf", list2);
		complexParam.put("asdfasdfas", list2);
		complexParam.put("asdfasdfasdfasdf", list2);
		complexParam.put("asdfasdfasdfasdfasdfasdasdfasdff", list2);
		
		System.out.println(CacheFilter.checksum(new Object[]{
				new Integer(10), 
				new Integer(100),
				complexParam}));
		
		long start = System.currentTimeMillis();
		for(int i=0; i<1000000; i++)
		{
			CacheFilter.checksum(new Object[]{
					new Integer(10), 
					new Integer(100),
					complexParam});
		}
		System.out.println("checksum cost: " + (System.currentTimeMillis() - start));
	}

}
