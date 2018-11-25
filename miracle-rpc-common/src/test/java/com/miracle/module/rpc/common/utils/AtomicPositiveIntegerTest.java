package com.miracle.module.rpc.common.utils;

import static org.junit.Assert.*;

import static org.hamcrest.core.Is.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AtomicPositiveIntegerTest {

	private AtomicPositiveInteger atomicPositiveInt;
	@Before
	public void setUp() throws Exception {
		atomicPositiveInt = new AtomicPositiveInteger(0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAdd()
	{
		atomicPositiveInt.set(1000);
		int ret = atomicPositiveInt.addAndGet(100);
		assertThat(ret, is(1100));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetNegativeInt()
	{
		atomicPositiveInt.set(-10);
	}
	
	@Test
	public void testAddFromMax()
	{
		atomicPositiveInt.set(Integer.MAX_VALUE);
		int ret = atomicPositiveInt.addAndGet(1);
		assertThat(ret, is(0));
	}
	
	@Test
	public void testDec()
	{
		atomicPositiveInt.set(10);
		assertThat(atomicPositiveInt.decrementAndGet(), is(9));
	}
	
	@Test
	public void testDecFromMin()
	{
		atomicPositiveInt.set(0);
		assertThat(atomicPositiveInt.decrementAndGet(), is(Integer.MAX_VALUE));
	}

}
