package com.miracle.module.rpc.config;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringConfigTest {

	static ApplicationContext ctx = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ctx = new ClassPathXmlApplicationContext("kkrpc-spring-config.xml");  
	}
	
//	<kkrpc:application id="myapp" name="test" author="wqj" version="1.0.0"></kkrpc:application>
	@Test
	public void testApplicationConfig() {		
		ApplicationConfig application = (ApplicationConfig) ctx.getBean("myapp");
		System.out.println(application.getAppName());
		System.out.println(application.getAppVersion());
		System.out.println(application.getAuthor());
		Assert.assertEquals("test", application.getAppName());
		Assert.assertEquals("wqj", application.getAuthor());
		Assert.assertEquals("1.0.0", application.getAppVersion());
	}


	
//	<kkrpc:registry id="myregistry" address="10.0.3.239:2181" connecttimeout="10000" sessiontimeout="30000"></kkrpc:registry>
	@Test
	public void testRegistryConfig() {
		RegistryConfig registry = ctx.getBean(RegistryConfig.class);
		System.out.println(registry.getId());
		System.out.println(registry.getAddress());
		System.out.println(registry.getSessconnTimeout());
		System.out.println(registry.getSessionTimeout());
		Assert.assertEquals("10.0.3.239:2181", registry.getAddress());
		Assert.assertTrue(10000 == registry.getSessconnTimeout());
		Assert.assertTrue(30000 == registry.getSessionTimeout());
	}
	
//	<kkrpc:consumer timeout="8000" retries="3" loadbalance="roundrobin" cluster="failover" async="false"
//	filter="ooo,ppp" listener="ttt,qqq" heartbeat="30000" heartbeattimeout="90000" version="1.0.0" connections="1"
//	sticky="false" reconnect="1000" application="myapp" registry="myregistry" group="api" iprouter="10.0.0.*"
//	host="10.0.0.2" port="2345" connecttimeout="3000" monitor="mymonitor"></kkrpc:consumer>
	@Test
	public void testConsumerConfig() {
		ConsumerConfig refConfig = ctx.getBean(ConsumerConfig.class);
		System.out.println(refConfig.getApplication().getAuthor());
		System.out.println(refConfig.getRegistry().getAddress());
		Assert.assertEquals("roundrobin", refConfig.getLoadBalance());
		Assert.assertEquals("failover", refConfig.getCluster());
		Assert.assertEquals("ooo,ppp", refConfig.getFilter());
		Assert.assertEquals("ttt,qqq", refConfig.getListener());
		Assert.assertEquals("1.0.0", refConfig.getVersion());
		Assert.assertEquals("api", refConfig.getGroup());
		Assert.assertEquals("10.0.0.*", refConfig.getIprouter());
		Assert.assertTrue(8000 == refConfig.getTimeout());
		Assert.assertTrue(3 == refConfig.getRetries());
		Assert.assertTrue(30000 == refConfig.getHeartbeat());
		Assert.assertTrue(90000 == refConfig.getHeartbeatTimeout());
		Assert.assertTrue(1 == refConfig.getConnections());
		Assert.assertTrue(1000 == refConfig.getReconnect());
		Assert.assertFalse(refConfig.getAsync());
		Assert.assertFalse(refConfig.isSticky());
		Assert.assertEquals("10.0.0.2", refConfig.getHost());
		Assert.assertTrue(2345 == refConfig.getPort());
		Assert.assertTrue(3000 == refConfig.getConnectTimeout());
		Assert.assertEquals("10.0.3.239:2181", refConfig.getRegistry().getAddress());
		Assert.assertEquals("wqj", refConfig.getApplication().getAuthor());
		Assert.assertEquals("mymonitor", refConfig.getMonitor());
	}
	
//	<kkrpc:provider timeout="8000" retries="3" loadbalance="roundrobin" cluster="failover" async="false"
//	filter="ooo,ppp" listener="ttt,qqq" heartbeat="30000" heartbeattimeout="90000" version="1.0.0" tps="10000" tpsinterval="60000"
//	application="myapp" registry="myregistry" host="10.0.0.2" port="2345" group="api" monitor="mymonitor"
//	workerthreads="8" iothreads="6" queuesize="10000"></kkrpc:provider>
	@Test
	public void testProviderConfig()
	{
		ProviderConfig refConfig = ctx.getBean(ProviderConfig.class);
		System.out.println(refConfig.getApplication().getAuthor());
		System.out.println(refConfig.getRegistry().getAddress());
		Assert.assertEquals("roundrobin", refConfig.getLoadBalance());
		Assert.assertEquals("failover", refConfig.getCluster());
		Assert.assertEquals("ooo,ppp", refConfig.getFilter());
		Assert.assertEquals("ttt,qqq", refConfig.getListener());
		Assert.assertEquals("1.0.0", refConfig.getVersion());
		Assert.assertEquals("api", refConfig.getGroup());
		Assert.assertTrue(10000 == refConfig.getTps());
		Assert.assertTrue(60000 == refConfig.getTpsInterval());
		Assert.assertTrue(8000 == refConfig.getTimeout());
		Assert.assertTrue(3 == refConfig.getRetries());
		Assert.assertTrue(30000 == refConfig.getHeartbeat());
		Assert.assertTrue(90000 == refConfig.getHeartbeatTimeout());
		Assert.assertTrue(8 == refConfig.getWorkerThreads());
		Assert.assertTrue(6 == refConfig.getIoThreads());
		Assert.assertFalse(refConfig.getAsync());
		Assert.assertEquals("10.0.0.2", refConfig.getHost());
		Assert.assertTrue(2345 == refConfig.getPort());
		Assert.assertTrue(10000 == refConfig.getQueueSize());
		Assert.assertEquals("10.0.3.239:2181", refConfig.getRegistry().getAddress());
		Assert.assertEquals("wqj", refConfig.getApplication().getAuthor());
		Assert.assertEquals("mymonitor", refConfig.getMonitor());
	}

	
//	<kkrpc:service interface="com.melot.module.kkrpc.config.Calculator" target="calcImp" host="10.0.3.108" port="2345">
// 	<kkrpc:method name="getKey" retries="5" timeout="3000"></kkrpc:method>
//	<kkrpc:method name="getName" retries="4" timeout="1000" cache="true" cachesize="1000" cachettl="300"></kkrpc:method>
//	</kkrpc:service>
	@Test
	public void testServiceConfig()
	{
//		@SuppressWarnings("unchecked")
//		ServiceBean<Calculator> serviceConfig = (ServiceBean<Calculator>) ctx.getBean("calc");
//		Assert.assertTrue(3000 == serviceConfig.getRpcConfig().getMethodParameter("getKey", "timeout", 8000));
//		Assert.assertTrue(1000 == serviceConfig.getRpcConfig().getMethodParameter("getName", "timeout", 8000));
//		Assert.assertTrue(8000 == serviceConfig.getRpcConfig().getMethodParameter("getName2", "timeout", 8000));
//		Assert.assertTrue(5 == serviceConfig.getRpcConfig().getMethodParameter("getKey", "retries", 3));
//		Assert.assertTrue(4 == serviceConfig.getRpcConfig().getMethodParameter("getName", "retries", 3));
//		Assert.assertEquals("true", serviceConfig.getRpcConfig().getMethodParameter("getName", "cache"));
//		System.out.println(serviceConfig.getRpcConfig().getMethodParameter("getName", "cache.size"));
//		Assert.assertTrue(1000 == serviceConfig.getRpcConfig().getMethodParameter("getName", "cache.size", 10000));
//		Assert.assertTrue(300 == serviceConfig.getRpcConfig().getMethodParameter("getName", "cache.ttl", 10000));
	}
}
