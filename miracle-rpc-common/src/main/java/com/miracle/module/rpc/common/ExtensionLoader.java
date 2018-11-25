package com.miracle.module.rpc.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.miracle.module.rpc.common.utils.StringUtils;

public class ExtensionLoader {
	
	  private static final ConcurrentMap<Class<?>, Map<String, Class<?>>> EXTENSION_CLSS = new ConcurrentHashMap<Class<?>, Map<String, Class<?>>>();

	  private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

	  private static final String KKRPC_EXT_DIR = "META-INF/";
	  
	  public static void clearCachedExtensionInstances(Class<?> infCls)
	  {
		  Map<String, Class<?>>  extClss = getExtensionClss(infCls);
		  if(extClss != null && extClss.size() > 0)
		  {
			  for(String name : extClss.keySet())
			  {
				  Class<?> extCls = extClss.get(name);
				  EXTENSION_INSTANCES.remove(extCls);
			  }
		  }
	  }
	  
	  private static Map<String, Class<?>> getClassesImplementing(Class<?> infCls)
	  {
		  String fileName = KKRPC_EXT_DIR + infCls.getName();
		  Map<String, Class<?>> extensionClsses = new HashMap<String, Class<?>>();
		  try {
			  Enumeration<java.net.URL> urls = ExtensionLoader.class.getClassLoader().getResources(fileName);
			  if(urls != null)
			  {
				  while (urls.hasMoreElements()) {
					  java.net.URL url = urls.nextElement();
					  Properties p = new Properties();
					  InputStream input = url.openStream();
					  if (input != null) {
						  try {
							  p.load(input);
						  } finally {
							  try {
								  input.close();
							  } catch (Throwable t) {}
						  }
					  }
					  for(Object obj : p.keySet())
					  {
						  String key = (String)obj;
						  String clsName = p.getProperty(key);
						  if(!StringUtils.isBlank(clsName))
						  {
							  try {
								  Class<?> cls = ExtensionLoader.class.getClassLoader().loadClass(clsName);
								  extensionClsses.put(key, cls);
							  } catch (ClassNotFoundException e) {
								  e.printStackTrace();
							  }
						  }
					  }
				  }
			  }
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
		  
		  
		  return extensionClsses;
	  }
	  
	  public static Map<String, Class<?>> getExtensionClss(Class<?> infCls)
	  {
		  if(!EXTENSION_CLSS.containsKey(infCls))
		  {
				Map<String, Class<?>> clss = getClassesImplementing(infCls);
				EXTENSION_CLSS.put(infCls, clss);
		  }
		  
		  return EXTENSION_CLSS.get(infCls);
	  }
	  
	  private static Object getExtentInstance(Map<String, Class<?>> extClss, String extName)
	  {
		  if(extClss != null && extClss.size() > 0)
		  {
			  for(String name : extClss.keySet())
			  {
				  if(name.equals(extName))
				  {
					  Class<?> cls = extClss.get(name);
					  if(!EXTENSION_INSTANCES.containsKey(cls))
					  {
						  try {
							Object retObj = cls.newInstance();
							EXTENSION_INSTANCES.put( cls, retObj);
						  } catch (InstantiationException e) {
							  // TODO Auto-generated catch block
							  e.printStackTrace();
						  } catch (IllegalAccessException e) {
							  // TODO Auto-generated catch block
							  e.printStackTrace();
						  }
					  }
					  return EXTENSION_INSTANCES.get(cls);
				  }
			  }
		  }
		  return null;
	  }
	  
	  public static List<Object> getExtensionInstances(Class<?> infCls, String... names)
	  {
		  List<Object> extInstances = new ArrayList<Object>();
		  Map<String, Class<?>>  extClss = getExtensionClss(infCls);
		  if(extClss != null && extClss.size() > 0)
		  {
			  for(String extName : names)
			  {
				  Object extInstance = getExtentInstance(extClss, extName);
				  if(extInstance != null)
				  {
					  extInstances.add(extInstance);
				  }
			  }
		  }
		  return extInstances;
	  }
}
