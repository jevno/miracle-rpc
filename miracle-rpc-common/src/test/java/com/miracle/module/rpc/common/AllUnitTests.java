package com.miracle.module.rpc.common;

import com.miracle.module.rpc.common.utils.CommonUtilTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	RpcConfigTest.class,
	ExtensionLoaderTest.class,
	CommonUtilTests.class
})
public class AllUnitTests {

}
