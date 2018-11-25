package com.miracle.module.rpc.config.spring;

import java.util.List;
import java.util.Map;

import com.miracle.module.rpc.config.ApplicationConfig;
import com.miracle.module.rpc.config.ProviderConfig;
import com.miracle.module.rpc.config.RegistryConfig;
import com.miracle.module.rpc.config.ServiceConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.miracle.module.rpc.common.utils.NetUtils;
import com.miracle.module.rpc.common.utils.StringUtils;


public class ServiceBean<T> extends ServiceConfig<T> implements InitializingBean,
	DisposableBean, ApplicationContextAware{

	private static final Logger log = Logger.getLogger(ServiceBean.class);
	private transient ApplicationContext applicationContext;
	
	private volatile static Integer default_server_port = null;
    
	public ServiceBean()
	{
		super();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(this.getDefaultServiceConfig() == null)
		{
			Map<String, ProviderConfig> providerConfigMap =
					applicationContext == null ? null  : BeanFactoryUtils.beansOfTypeIncludingAncestors(
							applicationContext, ProviderConfig.class, false, false);
			if (providerConfigMap != null && providerConfigMap.size() > 0) {
				ProviderConfig providerConfig = null;
               
                if (providerConfigMap.size() > 1) {
                        throw new IllegalStateException("Duplicate consumer configs ~");
                }
                for(ProviderConfig config : providerConfigMap.values())
                {
                	providerConfig = config;
                    break;
                }
       
                if (providerConfig != null) {
                    this.setDefaultServiceConfig(providerConfig);
                }
            }
		}
		if (getApplication() == null
                && (this.getDefaultServiceConfig() == null || getDefaultServiceConfig().getApplication() == null)) {
			Map<String, ApplicationConfig> applicationConfigMap =
					applicationContext == null ? null  : BeanFactoryUtils.beansOfTypeIncludingAncestors(
							applicationContext, ApplicationConfig.class, false, false);
			if (applicationConfigMap != null && applicationConfigMap.size() > 0) {
				ApplicationConfig applicationConfig = null;
               
                if (applicationConfigMap.size() > 1) {
                        throw new IllegalStateException("Duplicate application configs ~");
                }
                for(ApplicationConfig config : applicationConfigMap.values())
                {
                	applicationConfig = config;
                    break;
                }
       
                if (applicationConfig != null) {
                    this.setApplication(applicationConfig);
                }
            }
		}
		if (getRegistry() == null
                && (this.getDefaultServiceConfig() == null || getDefaultServiceConfig().getRegistry() == null)) {
			Map<String, RegistryConfig> registryConfigMap =
					applicationContext == null ? null  : BeanFactoryUtils.beansOfTypeIncludingAncestors(
							applicationContext, RegistryConfig.class, false, false);
			if (registryConfigMap != null && registryConfigMap.size() > 0) {
				RegistryConfig registryConfig = null;
               
                if (registryConfigMap.size() > 1) {
                        throw new IllegalStateException("Duplicate registry configs ~");
                }
                for(RegistryConfig config : registryConfigMap.values())
                {
                	registryConfig = config;
                    break;
                }
                if (registryConfig != null) {
                    this.setRegistry(registryConfig);
                }
            }
		}
		
		String host = this.getHost() == null? this.getDefaultServiceConfig().getHost() : this.getHost();
		if(StringUtils.isBlank(host))
		{
			throw new IllegalStateException("<kkrpc:service> server host can not be null");
		}
		host = NetUtils.fixIpWithWildChar(host);
		List<String> ipList = NetUtils.getAllIps();
		if(ipList != null && !ipList.contains(host))
		{
			throw new IllegalStateException("<kkrpc:service> server host not a local address");
		}
		this.setHost(host);
		
		Integer port = this.getPort();
		if(port != null)
		{
			if(!NetUtils.isThisPortAvailable(port.toString()))
			{
				int serverPort = NetUtils.getAvailablePort(20000);
				port = serverPort;
				log.warn("<kkrpc:service> specify a occupied server port, we choose another one for you: " + port);
			}
		}
		else
		{
			if(default_server_port == null)
			{
				Integer defport = this.getDefaultServiceConfig().getPort();
				if(defport == null || !NetUtils.isThisPortAvailable(defport.toString()))
				{
					defport = NetUtils.getAvailablePort(20000);
				}
				default_server_port = defport;
			}
			port = default_server_port;
			log.warn("<kkrpc:service> not specify a independent server port, we use default shared one for you: " + port);
		}
		this.setPort(port);
		
		this.initialize(); //early init
	}

}
