package com.miracle.module.rpc.common.utils;

import org.junit.Test;

public class UnsafeStringWriterTest {

	@SuppressWarnings("resource")
	@Test(expected=IllegalArgumentException.class)
	public void testNegativeArgumentsConstructor() {
		UnsafeStringWriter writer = new UnsafeStringWriter(-10);
		writer.toString();
	}

}
