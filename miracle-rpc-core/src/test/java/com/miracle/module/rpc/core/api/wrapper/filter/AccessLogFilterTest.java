package com.miracle.module.rpc.core.api.wrapper.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcContext;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccessLogFilterTest {

	interface TestInfo
	{
		public int echoString();
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		RpcContext.getContext().setRemoteAddress(new InetSocketAddress("10.0.1.160", 20000));
		AccessLogFilter filter = new AccessLogFilter();
		RpcResult result = mock(RpcResult.class);
		@SuppressWarnings("unchecked")
		Invoker<TestInfo> invoker = mock(Invoker.class);
		RpcRequest request = mock(RpcRequest.class);
		RpcConfig config = mock(RpcConfig.class);
		when(config.getParameter(Constants.VERSION_KEY)).thenReturn("1.0.0");
		when(config.getParameter(Constants.GROUP_KEY)).thenReturn("api");
		when(config.getIp()).thenReturn("10.0.1.219");
		when(config.getPort()).thenReturn(30001);
		when(invoker.getInterface()).thenReturn(TestInfo.class);
		when(invoker.getConfig()).thenReturn(config);
		when(invoker.invoke(request)).thenReturn(result);
		when(request.getMethodName()).thenReturn("echo");
		when(request.getId()).thenReturn(1001);
		Object[] params = new Object[]{"aaa", "bbb"};
		when(request.getArguments()).thenReturn(params);
		
		when(result.getId()).thenReturn(1001);
		when(result.hasException()).thenReturn(false);
		List<Object> listRet = new ArrayList<Object>();
		listRet.add("String");
		listRet.add(null);
		listRet.add(900);
		Map<Object, Object> mapRet = new HashMap<Object, Object>();
		mapRet.put("String", "asdfasdf");
		mapRet.put("NUll", null);
		mapRet.put(300, 400);
		when(result.getValue()).thenReturn(mapRet);
		//when(result.hasException()).thenReturn(true);
		//when(result.getException()).thenReturn(new NullPointerException("test"));
		//when(result.getProtoErr()).thenReturn(new RpcProtoError("NullPointerException", "test"));
		
		filter.invoke(invoker, request);
	}

}
