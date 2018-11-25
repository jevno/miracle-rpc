package com.miracle.module.rpc.common.utils;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class TypesUtilTest {

	@Test
	public void testIsPrimitive() {
		boolean actual = TypesUtil.isPrimitive(Integer.class);
		Assert.assertTrue("Integer is primitive type", actual);
		Assert.assertTrue("Int is primitive type", TypesUtil.isPrimitive(int.class));
		Assert.assertFalse("Arraylist is not primitive type", TypesUtil.isPrimitive(ArrayList.class));
	}

	@Test
	public void testConvertPrimitive() {
		Assert.assertEquals(2.0, TypesUtil.convertPrimitive(Double.class, "2.0"));
		Assert.assertEquals(true, TypesUtil.convertPrimitive(Boolean.class, "true"));
	}

}
