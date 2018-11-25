package com.miracle.module.rpc.starter;

import java.util.Map;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.common.utils.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class Main {
	private static Logger log = Logger.getLogger(Main.class);
	
	private static void startLogger(String loggerFile) {
		String log4jPath = null;
	    if(loggerFile == null) {
            log4jPath = ConfigUtils.getResourceFullPath("log4j.properties");
            if(!ConfigUtils.isValidFileName(log4jPath))
            {
            	log4jPath = ConfigUtils.getResourceFullPath("log4j.xml");
            }
            
            //try conf directory
            if(!ConfigUtils.isValidFileName(log4jPath))
            {
            	log4jPath = ConfigUtils.getResourceFullPath("conf/log4j.properties");
            	if(!ConfigUtils.isValidFileName(log4jPath))
                {
                	log4jPath = ConfigUtils.getResourceFullPath("conf/log4j.xml");
                }
            }
        }
        else if(!StringUtils.isBlank(loggerFile)) {
        	log4jPath = ConfigUtils.getResourceFullPath(loggerFile);
        }
	    if(ConfigUtils.isValidFileName(log4jPath))
	    {
	    	PropertyConfigurator.configureAndWatch(log4jPath);
	    }
	    else
	    {
	    	log.error("Not found appropriate log4j configure file.");
	    }
	}
	
	private static void start() {
		start(null,null,null);
	}
	
	private static void start(String loggerFile, String[] fileSystemXmlFiles, String[] classPathXmlFiles)  {
	    startLogger(loggerFile);
	    
        if (((fileSystemXmlFiles == null) || (fileSystemXmlFiles.length < 1)) && ((classPathXmlFiles == null) || (classPathXmlFiles.length < 1))) {
			init(null, new String[] { "application-context.xml", "spring-bean-container*.xml", "server-module-*.xml" });
        }
        else {
        	init(fileSystemXmlFiles, classPathXmlFiles);
        }
	}

	private static byte[] lock = new byte[0];
	private static AbstractXmlApplicationContext context = null;

	public static void init(String[] fileSystemXmlFiles,
							String[] classPathXmlFiles) {
		if ((fileSystemXmlFiles == null || fileSystemXmlFiles.length < 1)
				&& (classPathXmlFiles == null || classPathXmlFiles.length < 1)) {
			assertBeanFactoryValid();
			return;
		}
		synchronized (lock) {
			if (context == null) {
				if (fileSystemXmlFiles != null && fileSystemXmlFiles.length > 0) {
					context = new FileSystemXmlApplicationContext(
							fileSystemXmlFiles, false);
				}
				if (classPathXmlFiles != null && classPathXmlFiles.length > 0) {
					context = new ClassPathXmlApplicationContext(
							classPathXmlFiles, false, context);
				}
				if(context != null)
				{
					context.refresh();
				}

				if (context != null) {
					context.setAllowBeanDefinitionOverriding(false);
					if (context.getBeanDefinitionCount() < 1) {
						context.close();
						context = null;
					}
				}
			}
		}

		assertBeanFactoryValid();
	}


	/**
	 * Assert that this MelotBeanFactory is valid
	 */
	private static void assertBeanFactoryValid() {
		synchronized (lock) {
			if (context == null) {
				throw new IllegalStateException(
						"Failed to initialize the MelotBeanFactory");
			}
		}
	}
	
	public static void startClient()
	{
		start();
	}
	
	public static void startServer()
	{
		start();
		holdTheWorld();
	}
	
	public static void startClient(String... classPathXmlFiles)
	{
		start(null, null, classPathXmlFiles);
	}
	
	public static void startServer(String... classPathXmlFiles)
	{
		start(null, null, classPathXmlFiles);
		holdTheWorld();
	}
	
	public static void holdTheWorld()
	{
		 synchronized (Main.class) {
            try {
                Main.class.wait();
            } catch (Throwable e) {
            }
		 }
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified
	 * bean.
	 *
	 * @param name
	 *            the name of the bean to retrieve
	 * @return an instance of the bean
	 */
	public static Object getBean(String name) {
		assertBeanFactoryValid();
		try {
			if (context.containsBean(name)) {
				return context.getBean(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified
	 * bean.
	 *
	 * @param name
	 *            the name of the bean to retrieve
	 * @param requiredType
	 *            type the bean must match. Can be an interface or superclass of
	 *            the actual class, or null for any match. For example, if the
	 *            value is Object.class, this method will succeed whatever the
	 *            class of the returned instance
	 * @return an instance of the bean
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name, Class<T> requiredType) {
		assertBeanFactoryValid();
		try {
			if (context.containsBean(name)) {
				Object object = context.getBean(name);
				if (requiredType != null && requiredType.isInstance(object)) {
					return (T) object;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified
	 * bean.
	 *
	 * @param requiredType
	 *            type the bean must match; can be an interface or superclass.
	 *            null is disallowed.
	 * @return an instance of the bean
	 */
	public static <T> T getBean(Class<T> requiredType) {
		assertBeanFactoryValid();
		try {
			return context.getBean(requiredType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return the bean instances that match the given object type (including
	 * subclasses), judging from either bean definitions or the value of
	 * getObjectType in the case of FactoryBeans.
	 *
	 * @param requiredType
	 *            the class or interface to match, or null for all concrete
	 *            beans
	 * @return a Map with the matching beans, containing the bean names as keys
	 *         and the corresponding bean instances as values
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
		assertBeanFactoryValid();
		try {
			return context.getBeansOfType(requiredType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
