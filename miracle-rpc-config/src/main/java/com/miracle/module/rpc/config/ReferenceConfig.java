package com.miracle.module.rpc.config;


import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.ConfigUtils;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.common.utils.NetUtils;
import com.miracle.module.rpc.config.annotation.Parameter;
import com.miracle.module.rpc.core.api.CglibProxyFactory;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.Protocol;
import com.miracle.module.rpc.core.impl.ProtocolFactory;
import com.miracle.module.rpc.core.impl.injvm.InjvmProtocol;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenceConfig<T> extends AbstractReferenceConfig{
	
	private static Logger logger = Logger.getLogger(ReferenceConfig.class);
	
	private ConsumerConfig defaultConsumerConfig;
	private String interfaceName;
	private Class<?> interfaceClass;
	private List<MethodConfig> methods;
	private RpcConfig config;
	
    // 接口代理类引用
    private volatile T ref;

    private volatile Invoker<?> invoker;

    private volatile boolean initialized;

    private volatile boolean destroyed;

	public ConsumerConfig getDefaultReferenceConfig() {
		return defaultConsumerConfig;
	}

	public void setDefaultReferenceConfig(
			ConsumerConfig defaultReferenceConfig) {
		this.defaultConsumerConfig = defaultReferenceConfig;
	}
	
	public RpcConfig getRpcConfig()
	{
		return config;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public List<MethodConfig> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodConfig> methods) {
		this.methods = methods;
	}

	public synchronized T get() {
		if(destroyed)
		{
			throw new IllegalStateException("Already destroyed.");
		}
		if(ref == null)
		{
			initialize();
		}
		return ref;
	}
	
	private void initialize()
	{
		if(initialized)
			return;
		initialized = true;
		
		if(StringUtils.isEmpty(this.interfaceName))
		{
			throw new IllegalStateException("InterfaceName not allowed to be empty.");
		}
		try {
			interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
			        .getContextClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		fixDefaultConsumerConfig();
		appendProperties(this);
		if(defaultConsumerConfig != null)
		{
			if(this.application == null)
			{
				this.application = defaultConsumerConfig.application;
			}
			if(this.registry == null)
			{
				this.registry = defaultConsumerConfig.registry;
			}
		}
		this.fixApplicationConfig();
		this.fixRegistryConfig();
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(Constants.INTERFACE_KEY, this.interfaceName);
		paramMap.put(Constants.SIDE_KEY, Constants.CONSUMER_SIDE);
		if (ConfigUtils.getPid() > 0) {
			paramMap.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
	    }
		paramMap.put(Constants.LOCAL_ADDRESS_KEY, NetUtils.getLocalAddress().getHostAddress());
		paramMap.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
		
		appendParameters(paramMap, this.application);
		appendParameters(paramMap, this.registry);
		appendParameters(paramMap, this.defaultConsumerConfig, Constants.DEFAULT_KEY_PREFIX);
		appendParameters(paramMap, this);
		if(methods != null && methods.size() > 0)
		{
			for(MethodConfig method: methods)
			{
				appendParameters(paramMap, method, method.getName());
			}
		}
		
		config = new RpcConfig(paramMap);
		logger.info("prepare reference rpconfig: " + config);
		ref = createProxy(config);
	}
	
	@SuppressWarnings("unchecked")
	private T createProxy(RpcConfig rpconfig)
	{
		final InjvmProtocol injvmProtocol = InjvmProtocol.getInjvmProtocol();
		boolean useLocal = injvmProtocol.isInjvmRefer(rpconfig);
		if(useLocal)
		{
			invoker = injvmProtocol.refer(interfaceClass, rpconfig);
		}
		else
		{
			final Protocol refProtocol = ProtocolFactory.createProtocol(rpconfig);
			invoker = refProtocol.refer(interfaceClass, rpconfig);
		}
		
		return (T) CglibProxyFactory.getProxy(invoker);
	}

	@Parameter(excluded = true)
	public boolean isInitialized() {
		return initialized;
	}

	@Parameter(excluded = true)
	public boolean isDestroyed() {
		return destroyed;
	}
	
	public synchronized void destroy()
	{
		if(ref == null) {
			return;
		}
		if(destroyed) {
			return;
		}
		if(invoker == null) {
			return;
		}
		try
		{
			invoker.destroy();
		}
		catch(Throwable t)
		{
			logger.warn("unexpected err when destroy invoker: " + this.interfaceName, t);
		}
		invoker = null;
		ref = null;
	}
	
	private void fixDefaultConsumerConfig()
	{
		if(defaultConsumerConfig == null)
		{
			defaultConsumerConfig = new ConsumerConfig();
		}
		defaultConsumerConfig.fixDefaultReferenceConfig();
	}
}
