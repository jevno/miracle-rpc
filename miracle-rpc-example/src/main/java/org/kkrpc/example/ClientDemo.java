package org.kkrpc.example;

import com.miracle.module.rpc.config.ApplicationConfig;
import com.miracle.module.rpc.config.ConsumerConfig;
import com.miracle.module.rpc.config.ReferenceConfig;
import com.miracle.module.rpc.config.RegistryConfig;
import com.miracle.module.rpc.core.api.RpcContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ClientDemo {
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		ConsumerConfig defaultReferenceConfig = new ConsumerConfig();
		ApplicationConfig appCfg = new ApplicationConfig();
		appCfg.setAuthor("wqj");
		appCfg.setAppName("calc_client");
		appCfg.setAppVersion("1.0.0");
		defaultReferenceConfig.setApplication(appCfg);
		RegistryConfig regCfg = new RegistryConfig();
		regCfg.setAddress("10.0.3.239:2181");
		regCfg.setSessconnTimeout(10000);
		regCfg.setSessionTimeout(30000);
		defaultReferenceConfig.setRegistry(regCfg);
		defaultReferenceConfig.setConnectTimeout(5000);
		
		ReferenceConfig<Calculator> refConfig = new ReferenceConfig<Calculator>();
		refConfig.setDefaultReferenceConfig(defaultReferenceConfig);
		refConfig.setInterfaceName(Calculator.class.getName());
		refConfig.setTimeout(80000);
		refConfig.setConnections(1);
		refConfig.setVersion("1.0.0");
		refConfig.setHeartbeat(3000);
//		refConfig.setAsync(true);
		
		RpcContext.getContext().asyncBegin();
		Calculator calculator = refConfig.get();
		calculator.add(5, 5);
		Future<Integer> sumFuture = RpcContext.getContext().getFuture();
		calculator.add(12, 23);
		Future<Integer> sum2Future = RpcContext.getContext().getFuture();
		System.out.println("sum is " + sumFuture.get());
		System.out.println("sum2 is " + sum2Future.get());
		RpcContext.getContext().asyncEnd();
		
		int count = 0;
		while(count++ < 1000)
		{
			try{
				System.out.println("sum3 is " + calculator.add(20, 202));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			Thread.sleep(2000);
		}
		
		Thread.currentThread().join();
		
		refConfig.destroy();
	}
}
