package com.miracle.module.rpc.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ConfigUtils {
	 private static final Logger logger = Logger.getLogger(ConfigUtils.class);
	    
    public static boolean isNotEmpty(String value) {
        return ! isEmpty(value);
    }
	
	public static boolean isEmpty(String value) {
		return value == null || value.length() == 0 
    			|| "false".equalsIgnoreCase(value) 
    			|| "0".equalsIgnoreCase(value) 
    			|| "null".equalsIgnoreCase(value) 
    			|| "N/A".equalsIgnoreCase(value);
	}
	
	public static boolean isDefault(String value) {
		return "true".equalsIgnoreCase(value) 
				|| "default".equalsIgnoreCase(value);
	}
	

    private static Pattern VARIABLE_PATTERN = Pattern.compile(
            "\\$\\s*\\{?\\s*([\\._0-9a-zA-Z]+)\\s*\\}?");
    
	public static String replaceProperty(String expression, Map<String, String> params) {
        if (expression == null || expression.length() == 0 || expression.indexOf('$') < 0) {
            return expression;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) { // 逐个匹配
            String key = matcher.group(1);
            String value = System.getProperty(key);
            if (value == null && params != null) {
                value = params.get(key);
            }
            if (value == null) {
                value = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
	
    private static volatile Properties PROPERTIES;
    
    public static Properties getProperties() {
        if (PROPERTIES == null) {
            synchronized (ConfigUtils.class) {
                if (PROPERTIES == null) {
                    String path = System.getProperty(Constants.KKRPC_PROPERTIES_KEY);
                    if (path == null || path.length() == 0) {
                        path = System.getenv(Constants.KKRPC_PROPERTIES_KEY);
                        if (path == null || path.length() == 0) {
                            path = Constants.DEFAULT_KKRPC_PROPERTIES;
                        }
                    }
                    PROPERTIES = ConfigUtils.loadProperties(path, false, true);
                }
            }
        }
        return PROPERTIES;
    }
    
    public static void addProperties(Properties properties) {
        if (properties != null) {
            getProperties().putAll(properties);
        }
    }
    
    public static void setProperties(Properties properties) {
        if (properties != null) {
            PROPERTIES = properties;
        }
    }
    
	public static String getProperty(String key) {
	    return getProperty(key, null);
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && value.length() > 0) {
            return value;
        }
        Properties properties = getProperties();
        return replaceProperty(properties.getProperty(key, defaultValue), (Map)properties);
    }
    
    public static Properties loadProperties(String fileName) {
        return loadProperties(fileName, false, false);
    }
    
    public static Properties loadProperties(String fileName, boolean allowMultiFile) {
        return loadProperties(fileName, allowMultiFile, false);
    }
    
	/**
	 * Load properties file to {@link Properties} from class path.
	 * 
	 * @param fileName properties file name. for example: <code>dubbo.properties</code>, <code>METE-INF/conf/foo.properties</code>
	 * @param allowMultiFile if <code>false</code>, throw {@link IllegalStateException} when found multi file on the class path.
     * @param optional is optional. if <code>false</code>, log warn when properties config file not found!s
	 * @return loaded {@link Properties} content. <ul>
	 * <li>return empty Properties if no file found.
	 * <li>merge multi properties file if found multi file
	 * </ul>
	 * @throws IllegalStateException not allow multi-file, but multi-file exsit on class path.
	 */
    public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
        Properties properties = new Properties();
        if (fileName.startsWith("/")) {
            try {
                FileInputStream input = new FileInputStream(fileName);
                try {
                    properties.load(input);
                } finally {
                    input.close();
                }
            } catch (Throwable e) {
                logger.warn("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage(), e);
            }
            return properties;
        }
        
        List<java.net.URL> list = new ArrayList<java.net.URL>();
        try {
            Enumeration<java.net.URL> urls = ClassHelper.getClassLoader().getResources(fileName);
            list = new ArrayList<java.net.URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable t) {
            logger.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
        }
        
        if(list.size() == 0) {
            if (! optional) {
                logger.warn("No " + fileName + " found on the class path.");
            }
            return properties;
        }
        
        if(! allowMultiFile) {
            if (list.size() > 1) {
                String errMsg = String.format("only 1 %s file is expected, but %d kkrpc.properties files found on class path: %s",
                        fileName, list.size(), list.toString());
                logger.warn(errMsg);
                // throw new IllegalStateException(errMsg); // see http://code.alibabatech.com/jira/browse/DUBBO-133
            }

            // fall back to use method getResourceAsStream
            try {
                properties.load(ClassHelper.getClassLoader().getResourceAsStream(fileName));
            } catch (Throwable e) {
                logger.warn("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage(), e);
            }
            return properties;
        }
        
        logger.info("load " + fileName + " properties file from " + list);
        
        for(java.net.URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input != null) {
                    try {
                        p.load(input);
                        properties.putAll(p);
                    } finally {
                        try {
                            input.close();
                        } catch (Throwable t) {}
                    }
                }
            } catch (Throwable e) {
                logger.warn("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage(), e);
            }
        }
        
        return properties;
    }

    private static int PID = -1;
    
    public static int getPid() {
        if (PID < 0) {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();  
                String name = runtime.getName(); // format: "pid@hostname"  
                PID = Integer.parseInt(name.substring(0, name.indexOf('@')));
            } catch (Throwable e) {
                PID = 0;
            }
        }
        return PID;  
    }

    private static boolean isInlineJarPath(URL url)
    {
    	String classPath = url.getPath();
    	try {
    		classPath = URLDecoder.decode(classPath, "utf-8");
    	} catch (UnsupportedEncodingException e) {
    		e.printStackTrace();
    	}

    	if (classPath.contains(".jar!")) {
    		return true;
    	}
     
    	return false;
    }
	 
	public static boolean isValidFileName(String fullFileName)
	{
		if(fullFileName == null || fullFileName.isEmpty())
			return false;
 
		File file=new File(fullFileName);  
		if(file != null && file.exists())
			return true;
		return false;
	}
	 
	public static String getResourceFullPath(String fileName) 
	{
		if (fileName.startsWith("/")) { //Already full path, nothing to do
			return fileName;
		}
		
		List<java.net.URL> list = new ArrayList<java.net.URL>();
		try {
		    Enumeration<java.net.URL> urls = ClassHelper.getClassLoader().getResources(fileName);
		    list = new ArrayList<java.net.URL>();
		    while (urls.hasMoreElements()) {
		        list.add(urls.nextElement());
		    }
		} catch (Throwable t) {
		    logger.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
		}
	        
	    List<String> pathList = new ArrayList<String>();
        if (list.size() >= 1) 
        {
           	for(URL url: list)
           	{
           		if(!isInlineJarPath(url) && isValidFileName(url.getPath()))
           		{
           			pathList.add(url.getPath());
           		}
           	}
        }

        if(pathList.size() == 1)
        {
        	return pathList.get(0);
        }
        if(pathList.size() > 1)
        {
           	logger.warn("Found muliple valid files " + pathList + " in all classpath.");
           	return pathList.get(0);
        }
           
         
        String userdirPath = System.getProperty("user.dir");
        String userdirFullName = userdirPath + fileName;
        if(isValidFileName(userdirFullName))
        {
        	return userdirFullName;
        }
        	
        return fileName;
	}
	 
	private ConfigUtils() {}
}
