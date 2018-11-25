package com.miracle.module.rpc.config.spring;

import java.util.Map;

import com.miracle.module.rpc.config.ApplicationConfig;
import com.miracle.module.rpc.config.ConsumerConfig;
import com.miracle.module.rpc.config.ReferenceConfig;
import com.miracle.module.rpc.config.RegistryConfig;
import com.miracle.module.rpc.config.annotation.Parameter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


@SuppressWarnings("rawtypes")
public class ReferenceBean<T> extends ReferenceConfig<T>
	implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean{

	private transient ApplicationContext applicationContext;
	
	public ReferenceBean()
	{
		super();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(this.getDefaultReferenceConfig() == null)
		{
			Map<String, ConsumerConfig> consumerConfigMap =
					applicationContext == null ? null  : BeanFactoryUtils.beansOfTypeIncludingAncestors(
							applicationContext, ConsumerConfig.class, false, false);
			if (consumerConfigMap != null && consumerConfigMap.size() > 0) {
				ConsumerConfig consumerConfig = null;
               
                if (consumerConfigMap.size() > 1) {
                        throw new IllegalStateException("Duplicate consumer configs ~");
                }
                for(ConsumerConfig config : consumerConfigMap.values())
                {
                    consumerConfig = config;
                    break;
                }
       
                if (consumerConfig != null) {
                    this.setDefaultReferenceConfig(consumerConfig);
                }
            }
		}
		if (getApplication() == null
                && (this.getDefaultReferenceConfig() == null || getDefaultReferenceConfig().getApplication() == null)) {
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
                && (this.getDefaultReferenceConfig() == null || getDefaultReferenceConfig().getRegistry() == null)) {
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
		Boolean bInit = this.getInit();
		if(bInit == null && getDefaultReferenceConfig() != null)
		{
			bInit = getDefaultReferenceConfig().getInit();
		}
		Boolean useLocal = this.getInjvm();
		if(bInit == null && getDefaultReferenceConfig() != null)
		{
			useLocal = getDefaultReferenceConfig().getInjvm();
		}
		if(bInit != null && bInit.booleanValue() && (useLocal == null || !useLocal.booleanValue()))
		{
			getObject(); //early init
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object getObject() throws Exception {
		return this.get();
	}

	@Override
	public Class getObjectType() {
		return this.getInterfaceClass();
	}

	@Override
	@Parameter(excluded = true)
	public boolean isSingleton() {
		return true;
	}

}
