package com.miracle.module.rpc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.ConfigUtils;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.common.utils.NetUtils;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.config.annotation.Parameter;
import com.miracle.module.rpc.core.api.CglibProxyFactory;
import com.miracle.module.rpc.core.api.Exporter;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.Protocol;
import com.miracle.module.rpc.core.impl.ProtocolFactory;
import com.miracle.module.rpc.core.impl.injvm.InjvmProtocol;
import org.apache.log4j.Logger;


public class ServiceConfig<T> extends AbstractServiceConfig{
	private static Logger logger = Logger.getLogger(ServiceConfig.class);
	private ProviderConfig defaultProviderConfig;
	private String interfaceName;
	private Class<?> interfaceClass;
	private T target;
	private List<MethodConfig> methods;
	private RpcConfig config;
    private volatile boolean initialized;

    private volatile boolean destroyed;
    
    private final List<Exporter<?>> exporters = new ArrayList<Exporter<?>>();
        
    @Parameter(excluded = true)
	public boolean isInitialized() {
		return initialized;
	}

    @Parameter(excluded = true)
	public boolean isDestroyed() {
		return destroyed;
	}

    public RpcConfig getRpcConfig()
    {
    	return config;
    }
    
	public synchronized void initialize()
	{
		if (destroyed) {
            throw new IllegalStateException("Already unexported!");
        }
        if (initialized) {
            return;
        }
        initialized = true;
        if(StringUtils.isEmpty(this.interfaceName))
        {
        	throw new IllegalStateException("InterfaceName not allowed to be empty!");
        }
        if(this.target == null)
        {
        	throw new IllegalStateException("Proxy targer not allowed to be null");
        }
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        
        fixDefaultProviderConfig();
        if(defaultProviderConfig != null)
        {
        	if(this.application == null)
        	{
        		this.application = defaultProviderConfig.application;
        	}
        	if(this.registry == null)
        	{
        		this.registry = defaultProviderConfig.registry;
        	}
        }
        this.fixApplicationConfig();
        this.fixRegistryConfig();
        appendProperties(this);
        
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        paramMap.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
        	paramMap.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }
        paramMap.put(Constants.LOCAL_ADDRESS_KEY, NetUtils.getLocalAddress().getHostAddress());
        paramMap.put(Constants.INTERFACE_KEY, this.interfaceName);
        appendParameters(paramMap, this.application);
        appendParameters(paramMap, this.registry);
        appendParameters(paramMap, this.defaultProviderConfig, Constants.DEFAULT_KEY_PREFIX);
        appendParameters(paramMap, this);
        
        if(methods != null && methods.size() > 0)
		{
			for(MethodConfig method: methods)
			{
				appendParameters(paramMap, method, method.getName());
			}
		}
        config = new RpcConfig(paramMap);
        logger.info("Prepare service rpconfig:" + config);
        doExport(config);
	}
	
	private void doExport(RpcConfig config)
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Invoker<?> invoker = CglibProxyFactory.getInvoker(target, (Class)interfaceClass, config);
		
		//export injvm first
		final Protocol injvmProtocol = InjvmProtocol.getInjvmProtocol();
		Exporter<?> injvmExporter = injvmProtocol.export(invoker);
		exporters.add(injvmExporter);
		
		final Protocol protocol = ProtocolFactory.createProtocol(config);
		Exporter<?> exporter = protocol.export(invoker);
		exporters.add(exporter);
	}
	
	public synchronized void destroy() 
	{
		if (! initialized) {
            return;
        }
        if (destroyed) {
            return;
        }
       
        if (exporters != null && exporters.size() > 0) {
    		for (Exporter<?> exporter : exporters) {
    			try {
                    exporter.unexport();
                } catch (Throwable t) {
                    logger.warn("unexpected err when unexport" + exporter, t);
                }
    		}
    		exporters.clear();
    	}
        
        destroyed = true;
	}

	public ProviderConfig getDefaultServiceConfig() {
		return defaultProviderConfig;
	}

	public void setDefaultServiceConfig(ProviderConfig defaultServiceConfig) {
		this.defaultProviderConfig = defaultServiceConfig;
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

	public T getTarget() {
		return target;
	}

	public void setTarget(T target) {
		this.target = target;
	}

	public List<MethodConfig> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodConfig> methods) {
		this.methods = methods;
	}
	
	private void fixDefaultProviderConfig()
	{
		if(defaultProviderConfig == null)
		{
			defaultProviderConfig = new ProviderConfig();
		}
		defaultProviderConfig.fixDefaultServiceConfig();
	}
}
