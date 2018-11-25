package com.miracle.module.rpc.config.spring.schema;


import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.config.*;
import com.miracle.module.rpc.config.spring.ReferenceBean;
import com.miracle.module.rpc.config.spring.ServiceBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RpcBeanDefinitionParser implements BeanDefinitionParser{
	private final Class<?> beanClass;

	private final boolean required;

	public RpcBeanDefinitionParser(Class<?> beanClass, boolean required) {
	    this.beanClass = beanClass;
	    this.required = required;
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// TODO Auto-generated method stub
		return parse(element, parserContext, beanClass, required);
	}

	private static BeanDefinition parse(Element element, ParserContext parserContext, 
		 Class<?> beanClass, boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");
		if ((id == null || id.length() == 0) && required) {
			String generatedBeanName = element.getAttribute("name");
			if (generatedBeanName == null || generatedBeanName.length() == 0) {
			    generatedBeanName = element.getAttribute("interface");
			}
			if (generatedBeanName == null || generatedBeanName.length() == 0) {
				generatedBeanName = beanClass.getSimpleName();
			}
		    id = generatedBeanName; 
		    int counter = 2;
		    while(parserContext.getRegistry().containsBeanDefinition(id)) {
		        id = generatedBeanName + (counter ++);
		    }
		}
		if (id != null && id.length() > 0) {
		    if (parserContext.getRegistry().containsBeanDefinition(id))  {
				throw new IllegalStateException("Duplicate spring bean id " + id);
			}
		    parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
		    beanDefinition.getPropertyValues().addPropertyValue("id", id);
		}
		if(ApplicationConfig.class.equals(beanClass))
		{
			parseApplication(element, parserContext, beanDefinition, required);
		}
		else if(RegistryConfig.class.equals(beanClass))
		{
			parseRegistry(element, parserContext, beanDefinition, required);
		}
		else if(ConsumerConfig.class.equals(beanClass))
		{
			parseConsumer(element, parserContext, beanDefinition, required);
		}
		else if(ProviderConfig.class.equals(beanClass))
		{
			parseProvider(element, parserContext, beanDefinition, required);
		}
		else if(ServiceBean.class.equals(beanClass))
		{
			parseService(element, parserContext, beanDefinition, required);
		}
		else if(ReferenceBean.class.equals(beanClass))
		{
			parseReference(element, parserContext, beanDefinition, required);
		}

		return beanDefinition;
	}
	
	private static void parseApplication(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		if(element.hasAttribute("appname"))
		{
			String name = element.getAttribute("appname");
			beanDefinition.getPropertyValues().addPropertyValue("appName", name);
		}
		if(element.hasAttribute("appversion"))
		{
			String version = element.getAttribute("appversion");
			beanDefinition.getPropertyValues().addPropertyValue("appVersion", version);
		}
		if(element.hasAttribute("author"))
		{
			String author = element.getAttribute("author");
			beanDefinition.getPropertyValues().addPropertyValue("author", author);
		}
	}
	
	private static void parseRegistry(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		if(element.hasAttribute("address"))
		{
			String address = element.getAttribute("address");
			beanDefinition.getPropertyValues().addPropertyValue("address", address);
		}
		if(element.hasAttribute("connecttimeout"))
		{
			String connecttimeout = element.getAttribute("connecttimeout");
			beanDefinition.getPropertyValues().addPropertyValue("sessconnTimeout", connecttimeout);
		}
		if(element.hasAttribute("sessiontimeout"))
		{
			String sessiontimeout = element.getAttribute("sessiontimeout");
			beanDefinition.getPropertyValues().addPropertyValue("sessionTimeout", sessiontimeout);
		}
	}
	
	private static void parserAbstractInvoker(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		if(element.hasAttribute("timeout"))
		{
			String timeout = element.getAttribute("timeout");
			beanDefinition.getPropertyValues().addPropertyValue("timeout", timeout);
		}
		if(element.hasAttribute("retries"))
		{
			String retries = element.getAttribute("retries");
			beanDefinition.getPropertyValues().addPropertyValue("retries", retries);
		}
		if(element.hasAttribute("loadbalance"))
		{
			String loadbalance = element.getAttribute("loadbalance");
			beanDefinition.getPropertyValues().addPropertyValue("loadBalance", loadbalance);
		}
		if(element.hasAttribute("cluster"))
		{
			String cluster = element.getAttribute("cluster");
			beanDefinition.getPropertyValues().addPropertyValue("cluster", cluster);
		}
		if(element.hasAttribute("async"))
		{
			String async = element.getAttribute("async");
			beanDefinition.getPropertyValues().addPropertyValue("async", async);
		}
		if(element.hasAttribute("failthreshold"))
		{
			String failthreshold = element.getAttribute("failthreshold");
			beanDefinition.getPropertyValues().addPropertyValue("failThreshold", failthreshold);
		}
		if(element.hasAttribute("volumethreshold"))
		{
			String volumethreshold = element.getAttribute("volumethreshold");
			beanDefinition.getPropertyValues().addPropertyValue("volumeThreshold", volumethreshold);
		}
		if(element.hasAttribute("halfopentimeout"))
		{
			String halfopentimeout = element.getAttribute("halfopentimeout");
			beanDefinition.getPropertyValues().addPropertyValue("halfOpenTimeout", halfopentimeout);
		}
		if(element.hasAttribute("closethreshold"))
		{
			String closethreshold = element.getAttribute("closethreshold");
			beanDefinition.getPropertyValues().addPropertyValue("closeThreshold", closethreshold);
		}
		if(element.hasAttribute("forceopen"))
		{
			String forceopen = element.getAttribute("forceopen");
			beanDefinition.getPropertyValues().addPropertyValue("forceOpen", forceopen);
		}
		if(element.hasAttribute("forceclose"))
		{
			String forceclose = element.getAttribute("forceclose");
			beanDefinition.getPropertyValues().addPropertyValue("forceClose", forceclose);
		}
		if(element.hasAttribute("cache"))
		{
			String cache = element.getAttribute("cache");
			beanDefinition.getPropertyValues().addPropertyValue("cache", cache);
		}
		if(element.hasAttribute("cachesize"))
		{
			String cachesize = element.getAttribute("cachesize");
			beanDefinition.getPropertyValues().addPropertyValue("cacheSize", cachesize);
		}
		if(element.hasAttribute("cachettl"))
		{
			String cachettl = element.getAttribute("cachettl");
			beanDefinition.getPropertyValues().addPropertyValue("cacheTtl", cachettl);
		}
		if(element.hasAttribute("degrade"))
		{
			String degrade = element.getAttribute("degrade");
			beanDefinition.getPropertyValues().addPropertyValue("degrade", degrade);
		}
		if(element.hasAttribute("tps"))
		{
			String tps = element.getAttribute("tps");
			beanDefinition.getPropertyValues().addPropertyValue("tps", tps);
		}
		if(element.hasAttribute("tpsinterval"))
		{
			String tpsinterval = element.getAttribute("tpsinterval");
			beanDefinition.getPropertyValues().addPropertyValue("tpsInterval", tpsinterval);
		}
		if(element.hasAttribute("threadsname"))
		{
			String tpsinterval = element.getAttribute("threadsname");
			beanDefinition.getPropertyValues().addPropertyValue("threadsName", tpsinterval);
		}
		if(element.hasAttribute("threadscoresize"))
		{
			String tpsinterval = element.getAttribute("threadscoresize");
			beanDefinition.getPropertyValues().addPropertyValue("threadsCoreSize", tpsinterval);
		}
		if(element.hasAttribute("threadsqueuesize"))
		{
			String tpsinterval = element.getAttribute("threadsqueuesize");
			beanDefinition.getPropertyValues().addPropertyValue("threadsQueueSize", tpsinterval);
		}
		if(element.hasAttribute("semaphoreconcurrent"))
		{
			String tpsinterval = element.getAttribute("semaphoreconcurrent");
			beanDefinition.getPropertyValues().addPropertyValue("semaphoreConcurrent", tpsinterval);
		}
	}
	
	private static void parseInterface(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parserAbstractInvoker(element, parserContext, beanDefinition, required);
		if(element.hasAttribute("filter"))
		{
			String filter = element.getAttribute("filter");
			beanDefinition.getPropertyValues().addPropertyValue("filter", filter);
		}
		if(element.hasAttribute("monitor"))
		{
			String monitor = element.getAttribute("monitor");
			beanDefinition.getPropertyValues().addPropertyValue("monitor", monitor);
		}
		if(element.hasAttribute("listener"))
		{
			String listener = element.getAttribute("listener");
			beanDefinition.getPropertyValues().addPropertyValue("listener", listener);
		}
		if(element.hasAttribute("heartbeat"))
		{
			String heartbeat = element.getAttribute("heartbeat");
			beanDefinition.getPropertyValues().addPropertyValue("heartbeat", heartbeat);
		}
		if(element.hasAttribute("heartbeattimeout"))
		{
			String heartbeattimeout = element.getAttribute("heartbeattimeout");
			beanDefinition.getPropertyValues().addPropertyValue("heartbeatTimeout", heartbeattimeout);
		}
		if(element.hasAttribute("group"))
		{
			String group = element.getAttribute("group");
			beanDefinition.getPropertyValues().addPropertyValue("group", group);
		}
		if(element.hasAttribute("rule"))
		{
			String rule = element.getAttribute("rule");
			beanDefinition.getPropertyValues().addPropertyValue("rule", rule);
		}
		if(element.hasAttribute("rulearg"))
		{
			String rulearg = element.getAttribute("rulearg");
			beanDefinition.getPropertyValues().addPropertyValue("ruleArg", rulearg);
		}
		if(element.hasAttribute("application"))
		{
			String application = element.getAttribute("application");
			RuntimeBeanReference applicationRef = new RuntimeBeanReference(application);
			beanDefinition.getPropertyValues().addPropertyValue("application", applicationRef);
		}
		if(element.hasAttribute("registry"))
		{
			String registry = element.getAttribute("registry");
			RuntimeBeanReference registryRef = new RuntimeBeanReference(registry);
			beanDefinition.getPropertyValues().addPropertyValue("registry", registryRef);
		}
	}
	
	private static void parseAbstractReference(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseInterface(element, parserContext, beanDefinition, required);
		if(element.hasAttribute("host"))
		{
			String host = element.getAttribute("host");
			beanDefinition.getPropertyValues().addPropertyValue("host", host);
		}
		if(element.hasAttribute("port"))
		{
			String port = element.getAttribute("port");
			beanDefinition.getPropertyValues().addPropertyValue("port", port);
		}
		if(element.hasAttribute("connecttimeout"))
		{
			String connecttimeout = element.getAttribute("connecttimeout");
			beanDefinition.getPropertyValues().addPropertyValue("connectTimeout", connecttimeout);
		}
		if(element.hasAttribute("version"))
		{
			String version = element.getAttribute("version");
			beanDefinition.getPropertyValues().addPropertyValue("version", version);
		}
		if(element.hasAttribute("connections"))
		{
			String connections = element.getAttribute("connections");
			beanDefinition.getPropertyValues().addPropertyValue("connections", connections);
		}
		if(element.hasAttribute("sticky"))
		{
			String sticky = element.getAttribute("sticky");
			beanDefinition.getPropertyValues().addPropertyValue("sticky", sticky);
		}
		if(element.hasAttribute("reconnect"))
		{
			String reconnect = element.getAttribute("reconnect");
			beanDefinition.getPropertyValues().addPropertyValue("reconnect", reconnect);
		}
		if(element.hasAttribute("iprouter"))
		{
			String iprouter = element.getAttribute("iprouter");
			beanDefinition.getPropertyValues().addPropertyValue("iprouter", iprouter);
		}
		if(element.hasAttribute("init"))
		{
			String init = element.getAttribute("init");
			beanDefinition.getPropertyValues().addPropertyValue("init", init);
		}
		if(element.hasAttribute("injvm"))
		{
			String injvm = element.getAttribute("injvm");
			beanDefinition.getPropertyValues().addPropertyValue("injvm", injvm);
		}
	}
	
	private static void parseAbstractService(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseInterface(element, parserContext, beanDefinition, required);
		if(element.hasAttribute("host"))
		{
			String host = element.getAttribute("host");
			beanDefinition.getPropertyValues().addPropertyValue("host", host);
		}
		if(element.hasAttribute("port"))
		{
			String port = element.getAttribute("port");
			beanDefinition.getPropertyValues().addPropertyValue("port", port);
		}
		if(element.hasAttribute("workerthreads"))
		{
			String workerthreads = element.getAttribute("workerthreads");
			beanDefinition.getPropertyValues().addPropertyValue("workerThreads", workerthreads);
		}
		if(element.hasAttribute("iothreads"))
		{
			String iothreads = element.getAttribute("iothreads");
			beanDefinition.getPropertyValues().addPropertyValue("ioThreads", iothreads);
		}
		if(element.hasAttribute("queuesize"))
		{
			String queuesize = element.getAttribute("queuesize");
			beanDefinition.getPropertyValues().addPropertyValue("queueSize", queuesize);
		}
		if(element.hasAttribute("version"))
		{
			String version = element.getAttribute("version");
			beanDefinition.getPropertyValues().addPropertyValue("version", version);
		}
	}
	
	
	private static void parseProvider(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseAbstractService(element, parserContext, beanDefinition, required);
	}
	
	private static void parseConsumer(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseAbstractReference(element, parserContext, beanDefinition, required);
	}
	
	private static void parseService(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseAbstractService(element, parserContext, beanDefinition, required);
		String interfacename = null;
		if(element.hasAttribute("interface"))
		{
			interfacename = element.getAttribute("interface");
			beanDefinition.getPropertyValues().addPropertyValue("interfaceName", interfacename);
		}
		if(StringUtils.isBlank(interfacename))
		{
			throw new IllegalStateException("<kkrpc:service> interface attribute == null");
		}
		if(element.hasAttribute("provider"))
		{
			String provider = element.getAttribute("provider");
			RuntimeBeanReference providerRef = new RuntimeBeanReference(provider);
			beanDefinition.getPropertyValues().addPropertyValue("defaultProviderConfig", providerRef);
		}
		if(element.hasAttribute("target"))
		{
			String target = element.getAttribute("target");
			RuntimeBeanReference targetRef = new RuntimeBeanReference(target);
			beanDefinition.getPropertyValues().addPropertyValue("target", targetRef);
		}
		parseMethods(interfacename, element.getChildNodes(), beanDefinition, parserContext, required);
	}
	
	private static void parseReference(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseAbstractReference(element, parserContext, beanDefinition, required);
		String interfacename = null;
		if(element.hasAttribute("interface"))
		{
			interfacename = element.getAttribute("interface");
			beanDefinition.getPropertyValues().addPropertyValue("interfaceName", interfacename);
		}
		if(StringUtils.isBlank(interfacename))
		{
			throw new IllegalStateException("<kkrpc:reference> interface attribute == null");
		}
		if(element.hasAttribute("consumer"))
		{
			String consumer = element.getAttribute("consumer");
			RuntimeBeanReference consumerRef = new RuntimeBeanReference(consumer);
			beanDefinition.getPropertyValues().addPropertyValue("defaultConsumerConfig", consumerRef);
		}
		parseMethods(interfacename, element.getChildNodes(), beanDefinition, parserContext, required);
	}
	
	private static void parseMonitor(Element element, ParserContext parserContext, RootBeanDefinition beanDefinition, boolean required)
	{
		parseAbstractReference(element, parserContext, beanDefinition, required);
		String name = null;
		if(element.hasAttribute("name"))
		{
			name = element.getAttribute("name");
			beanDefinition.getPropertyValues().addPropertyValue("monitorName", name);
		}
		if(StringUtils.isBlank(name))
		{
			throw new IllegalStateException("<kkrpc:monitor> name attribute == null");
		}
		if(element.hasAttribute("interval"))
		{
			String interval = element.getAttribute("interval");
			beanDefinition.getPropertyValues().addPropertyValue("monitorInterval", interval);
		}
	}

	private static BeanDefinition parseMethod(String methodName, Element element,
			ParserContext parserContext, boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");
		if (StringUtils.isBlank(id)) {
			String generatedBeanName = "kkrpc_" + methodName;
			int counter = 2;
			id = generatedBeanName;
		    while(parserContext.getRegistry().containsBeanDefinition(id)) {
		        id = generatedBeanName + (counter ++);
		    }
		}
		beanDefinition.setBeanClass(MethodConfig.class);
		
		beanDefinition.getPropertyValues().addPropertyValue("name", methodName);
		parserAbstractInvoker(element, parserContext, beanDefinition, required);
		
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parseMethods(String interfaceName, NodeList nodeList, RootBeanDefinition beanDefinition,
			ParserContext parserContext, boolean required) {
		
		if (nodeList != null && nodeList.getLength() > 0) {
			ManagedList methods = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					if ("method".equals(node.getNodeName()) || "method".equals(node.getLocalName())) {
						String methodName = element.getAttribute("name");
						if (methodName == null || methodName.length() == 0) {
							throw new IllegalStateException("<kkrpc:method> name attribute == null");
						}
						if (methods == null) {
							methods = new ManagedList();
						}
						BeanDefinition methodBeanDefinition = parseMethod(methodName, ((Element) node),
								parserContext, required);
						String name = interfaceName + "." + methodName;
						BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(
								methodBeanDefinition, name);
						methods.add(methodBeanDefinitionHolder);
					}
				}
			}
			if (methods != null) {
				beanDefinition.getPropertyValues().addPropertyValue("methods", methods);
			}
		}
	}
}
