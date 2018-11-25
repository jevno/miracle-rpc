package org.kkrpc.example;


import com.miracle.module.rpc.config.ApplicationConfig;
import com.miracle.module.rpc.config.ProviderConfig;
import com.miracle.module.rpc.config.RegistryConfig;
import com.miracle.module.rpc.config.ServiceConfig;

public class ServerDemo {
	public static void main(String[] args)
	{
		ServiceConfig<Calculator> srvConfig = new ServiceConfig<Calculator>();
		ProviderConfig defaultServiceConfig = new ProviderConfig();
		ApplicationConfig appCfg = new ApplicationConfig();
		appCfg.setAuthor("wqj");
		appCfg.setAppName("calc_server");
		appCfg.setAppVersion("1.0.0");
		defaultServiceConfig.setApplication(appCfg);
		RegistryConfig regCfg = new RegistryConfig();
		regCfg.setAddress("10.0.3.239:2181");
		regCfg.setSessconnTimeout(10000);
		regCfg.setSessionTimeout(30000);
		defaultServiceConfig.setRegistry(regCfg);
		defaultServiceConfig.setHost("10.0.3.108");
		defaultServiceConfig.setPort(3456);
		defaultServiceConfig.setIoThreads(4);
		defaultServiceConfig.setQueueSize(10000);
		defaultServiceConfig.setWorkerThreads(4);
		
		
		srvConfig.setDefaultServiceConfig(defaultServiceConfig);
		srvConfig.setInterfaceName(Calculator.class.getName());
		srvConfig.setTarget(new CalculatorImpl());
		srvConfig.setTimeout(80000);
		srvConfig.setHeartbeat(3000);
		srvConfig.setVersion("1.0.0");
		
		srvConfig.initialize();
		
		 synchronized (ServerDemo.class) {
            while (true) {
                try {
                	ServerDemo.class.wait();
                } catch (Throwable e) {
                }
            }
        }
	}
}
