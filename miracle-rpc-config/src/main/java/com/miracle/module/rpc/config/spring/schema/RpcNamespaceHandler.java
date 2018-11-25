package com.miracle.module.rpc.config.spring.schema;

import com.miracle.module.rpc.config.ApplicationConfig;
import com.miracle.module.rpc.config.ConsumerConfig;
import com.miracle.module.rpc.config.ProviderConfig;
import com.miracle.module.rpc.config.RegistryConfig;
import com.miracle.module.rpc.config.spring.ReferenceBean;
import com.miracle.module.rpc.config.spring.ServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;



public class RpcNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		registerBeanDefinitionParser("application", new RpcBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("registry", new RpcBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("provider", new RpcBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new RpcBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("service", new RpcBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("reference", new RpcBeanDefinitionParser(ReferenceBean.class, false));
	}

}
