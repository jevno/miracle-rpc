package com.miracle.module.rpc.common.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TypesUtilTest.class, 
	UnsafeStringWriterTest.class, 
	StringUtilsTest.class, 
	StringUtilsCamelToSplitNameTest.class,
	AtomicPositiveIntegerTest.class,
	BytesTest.class})
public class CommonUtilTests {

}
