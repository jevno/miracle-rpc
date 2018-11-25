package com.miracle.module.rpc.common.utils;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringUtilsCamelToSplitNameTest {
	private String expected;
	private String target;
	
	public StringUtilsCamelToSplitNameTest(String exp, String input)
	{
		expected = exp;
		target = input;
	}
	
	@SuppressWarnings("rawtypes")
	@Parameters
	public static Collection testwords() {
		return Arrays.asList(new Object[][]{
				{"employee_info", "employeeInfo"},
				{"employee_a_info", "employeeAInfo"},
				{null, null}
		});
	}
	
	@Test
	public void camelToSplitNameTest()
	{
		assertEquals(expected, StringUtils.camelToSplitName(target, "_"));
	}
}
