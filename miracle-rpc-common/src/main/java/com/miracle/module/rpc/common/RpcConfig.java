package com.miracle.module.rpc.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;

public class RpcConfig {
	private Map<String, String> parameters;
	 
	public RpcConfig(Map<String, String> params)
	{
		if (params == null) {
			parameters = new HashMap<String, String>();
		} else {
			parameters = new HashMap<String, String>(params);
		}
		parameters = Collections.unmodifiableMap(parameters);
	}
	
	public String getParameter(String key) {
        String value = parameters.get(key);
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.DEFAULT_KEY_PREFIX + "." + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }
    public double getParameter(String key, double defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        return d;
    }
    
    public float getParameter(String key, float defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        return f;
    }

    public long getParameter(String key, long defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        return l;
    }
    
    public int getParameter(String key, int defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        return i;
    }

    public short getParameter(String key, short defaultValue) {

        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        return s;
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }
    
    public boolean hasMethodParameter(String method, String key)
    {
		String value = parameters.get(method + "." + key);
		if (value == null || value.length() == 0) {
			value = getParameter(key);
		}
		return value != null && value.length() > 0;
    }
    
    public String getMethodParameter(String method, String key) {
        String value = parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return getParameter(key);
        }
        return value;
    }

    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public double getMethodParameter(String method, String key, double defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        return d;
    }
    
    public float getMethodParameter(String method, String key, float defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        return f;
    }
    
    public long getMethodParameter(String method, String key, long defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        return l;
    }
    
    public int getMethodParameter(String method, String key, int defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        return i;
    }
    
    public short getMethodParameter(String method, String key, short defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        return s;
    }
    
    public boolean getMethodParameter(String method, String key, boolean defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        boolean b = Boolean.parseBoolean(value);
        return b;
    }
    
    @Override
    public String toString()
    {
    	if(parameters != null)
    	{
    		return parameters.toString();
    	}
    	return "";
    }
    
    public String toUniqueString(String[] ignoredKeys)
    {
    	Set<String> ignoredKeySet = null;
    	if(ignoredKeys != null && ignoredKeys.length > 0)
    	{
    		ignoredKeySet = new HashSet<String>(Arrays.asList(ignoredKeys));
    	}
    	else {
    		ignoredKeySet = new HashSet<String>();
    	}
    	
	    if (parameters !=null && parameters.size() > 0) {
	    	StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(parameters).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0) {
                	if(ignoredKeys != null && ignoredKeySet.contains(entry.getKey()))
                	{
                		continue;
                	}
                    if (first) {
                        first = false;
                    } else {
                    	builder.append("&");
                    }
                    builder.append(entry.getKey());
                    builder.append("=");
                    builder.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
            return builder.toString();
        }
	    return "";
    }
    
    public String getIp() {
    	return this.getParameter(Constants.HOST_KEY);
	}

    public int getPort() {
        return this.getParameter(Constants.PORT_KEY, Constants.DEFAULT_PORT);
    }

	public String getAddress() {
	    return getIp() == null ? "*:" + getPort() : getIp() + ":" + getPort();
	}
	
  	public RpcConfig addParameter(String key, String value) {
        if(value == null || value.length() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(this.parameters);
        map.put(key, value);
        return new RpcConfig(map);
    }
	    
    public RpcConfig addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcConfig addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcConfig addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }
    
    public RpcConfig addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }
    
    public RpcConfig addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }
    
    public RpcConfig addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcConfig addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }
    
    public RpcConfig addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }
	    
	/**
	 * Add parameters to a new url.
	 * 
	 * @param params
	 * @return A new URL 
	 */
    public RpcConfig addParameters(Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return this;
        }

        Map<String, String> map = new HashMap<String, String>(this.parameters);
        map.putAll(params);
        return new RpcConfig(map);
    }
	    
    public RpcConfig addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i ++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }
	    
	    
    public RpcConfig removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }
    
    public RpcConfig removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

	public RpcConfig removeParameters(String... keys) {
	    if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(this.parameters);
        for (String key : keys) {
            map.remove(key);
        }
       
        return new RpcConfig(map);
	}

    public Map<String, String> getParamsMap() {  
        return this.parameters;
    }
    
    public Map<String, String> getParamsMapExcludeDefault() {
    	Map<String, String> clonedMap = new HashMap<String, String>(this.parameters);
    	List<String> removeList = new ArrayList<String>();
    	Map<String, String> addMap = new HashMap<String, String>();
    	
    	for(String key : clonedMap.keySet())
    	{
    		if(key.startsWith("default."))
    		{
    			String defVal = clonedMap.get(key);
    			String trimedKey = key.substring("default.".length());
    			if(!clonedMap.containsKey(trimedKey))
    			{
    				addMap.put(trimedKey, defVal);
    			}
    			removeList.add(key);
    		}
    	}
    	
    	for(String remKey : removeList)
    	{
    		clonedMap.remove(remKey);
    	}
    	if(!addMap.isEmpty())
    	{
    		for(String tmpKey : addMap.keySet())
    		{
    			clonedMap.put(tmpKey, addMap.get(tmpKey));
    		}
    	}
    	
    	return clonedMap;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RpcConfig other = (RpcConfig) obj;

        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } 
        else if (!parameters.equals(other.parameters))
        {
            return false;   
        }
       
        return true;
    }
    
    public String toJsonString()
    {
    	Gson gson = new Gson();  
    	String jsonString = gson.toJson(this.parameters);
    	return jsonString;
    }
    
    public static RpcConfig parseFromJsonString(String jsonString)
    {
    	 Gson gson = new Gson();  
    	 Map<String, String> paramMap = gson.fromJson(jsonString, new TypeToken<Map<String, String>>(){}.getType());
    	 return new RpcConfig(paramMap);
    }
    
    public String getServiceKey()
    {
    	String serviceKey = null;
    	String interfaceName = this.getParameter(Constants.INTERFACE_KEY);
    	String[] words = interfaceName.split("\\.");
    	if(words != null && words.length > 0)
    	{
    		serviceKey = words[words.length-1].toLowerCase();
    	}
    	String version = this.getParameter(Constants.VERSION_KEY, Constants.DEFAULT_VERSION);
    	
    	return serviceKey+"_"+version;
    }
    
    public String getRegistryServices()
    {
    	String registries = this.getParameter(Constants.ADDRESS_KEY);
    	String[] registryArr = registries.split(",");
    	List<String> registryList = new ArrayList<String>();
    	Collections.addAll(registryList, registryArr);
    	Collections.sort(registryList);
    	return StringUtils.join(registryList, ",");
    }
    
    public boolean isProvider()
    {
    	String side = this.getParameter(Constants.SIDE_KEY);
    	return Constants.PROVIDER_SIDE.equals(side);
    }
    
    public boolean isConsumer()
    {
    	String side = this.getParameter(Constants.SIDE_KEY);
    	return Constants.CONSUMER_SIDE.equals(side);
    }
    
    public String getAppTag()
    {
    	String apptag = "";
    	String appName = this.getParameter(Constants.APP_NAME_KEY);
    	if(!StringUtils.isBlank(appName)) {
    		apptag += appName;
    	}
    	String appVersion = this.getParameter(Constants.APP_VERSION_KEY);
    	if(!StringUtils.isBlank(appVersion)){
    		apptag = apptag + "_" + appVersion;
    	}
    	String localAddr = this.getParameter(Constants.LOCAL_ADDRESS_KEY);
    	if(!StringUtils.isBlank(localAddr)) {
    		apptag = apptag + "_" + localAddr;
    	}
    	String pid = this.getParameter(Constants.PID_KEY);
    	if(!StringUtils.isBlank(pid)) {
    		apptag = apptag + "_" + pid;
    	}
    	
    	return apptag;
    }
}
