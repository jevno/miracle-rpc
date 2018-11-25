/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.miracle.module.rpc.core.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.miracle.module.rpc.common.ExtensionLoader;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.annotation.Activate;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.*;

/**
 * ListenerProtocol
 * 
 * @author william.liangf
 */
public class ProtocolFilterWrapper implements Protocol {

    private final Protocol protocol;

    public ProtocolFilterWrapper(Protocol protocol){
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    @Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        return protocol.export(buildInvokerChain(invoker, Constants.FILTER_KEY));
    }

    @Override
	public <T> Invoker<T> refer(Class<T> type, RpcConfig config) throws RpcException {
        return buildInvokerChain(protocol.refer(type, config), Constants.FILTER_KEY);
    }

    @Override
	public void destroy() {
    	ExtensionLoader.clearCachedExtensionInstances(Filter.class);
        protocol.destroy();
    }
    
	private static String[] getAllActivatedFilter(RpcConfig config, String[] filterNames)
    {
    	Set<String> filterSet = new HashSet<String>();
    	if(filterNames != null && filterNames.length > 0)
    	{
    		for(String filter : filterNames)
    			filterSet.add(filter);
    	}
    	Map<String, Class<?>> filterImpls = ExtensionLoader.getExtensionClss(Filter.class);
    	final Map<String, Activate> filterAnnotationMap = new HashMap<String, Activate>();
    	//add auto filters to applied filter set
    	for(Map.Entry<String, Class<?>> entry : filterImpls.entrySet())
    	{
    		String name = entry.getKey();
    		Class<?> implCls = entry.getValue();
    		Activate activate = implCls.getAnnotation(Activate.class);
    		if(activate != null)
    		{
    			if("true".equals(activate.auto()))
    			{
    				filterSet.add(name);
    			}
    			filterAnnotationMap.put(name, activate);
    		}
    	}
    	//match group attribute of Activate annotation
    	String group = config.getParameter(Constants.SIDE_KEY);
    	Set<String> newFilterSet = new HashSet<String>(filterSet);
    	for(String filterName : filterSet)
    	{
    		Activate activate = filterAnnotationMap.get(filterName);
    		if(activate != null)
    		{
    			String[] groups = activate.group();
    			List<String> grouplist = Arrays.asList(groups);
    			if(grouplist.contains(group))
    			{
    				continue;
    			}
    		}
    		newFilterSet.remove(filterName);
    	}
    	filterSet = newFilterSet;
    	//match value attribute of Activate annotation
    	newFilterSet = new HashSet<String>(filterSet);
    	for(String filterName : filterSet)
    	{
    		Activate activate = filterAnnotationMap.get(filterName);
    		if(activate != null)
    		{
    			String[] values = activate.value();
    			if(values != null && values.length > 0)
    			{
    				boolean bExist = false;
    				for(String tmpVal : values)
    				{
    					if(config.getParameter(tmpVal) != null)
    					{
    						bExist = true;
    						break;
    					}
    				}
    				if(!bExist)
    				{
    					newFilterSet.remove(filterName);
    				}
    			}    			
    		}
    	}
    	filterSet = newFilterSet;
    	
    	//sort filter array by order attribute of Activate annotation 
    	class FilterSortHelper implements Comparable<FilterSortHelper>
    	{
    		private String name;
    		private final int order;
    		public FilterSortHelper(String name, boolean isProvider)
    		{
    			this.name = name;
    			Activate activate = filterAnnotationMap.get(name);
    			if(activate != null)
    			{
    				if(isProvider) {
    					this.order = activate.providerorder();
    				}
    				else {
    					this.order = activate.consumerorder();
    				}
    			}
    			else
    			{
    				this.order = 0;
    			}
    		}
    		public String getName()
    		{
    			return this.name;
    		}
			@Override
			public int compareTo(FilterSortHelper o) {
				
				return this.order - o.order;
			}
    		
    	}
    	List<FilterSortHelper> helperList = new ArrayList<FilterSortHelper>();
    	for(String filterName : filterSet)
    	{
    		helperList.add(new FilterSortHelper(filterName, config.isProvider()));
    	}
    	Collections.sort(helperList);
    	
    	String[] orderedFilter = new String[filterSet.size()];
    	for(int i=0; i<helperList.size(); i++)
    	{
    		orderedFilter[i] = helperList.get(i).getName();
    	}
    	
    	return orderedFilter;
    }

    private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key) {
        Invoker<T> last = invoker;
        String filterNames = invoker.getConfig().getParameter(key);
        
        String[] nameArr = null;
        if(!StringUtils.isEmpty(filterNames))
        {
        	nameArr = filterNames.split(",");
        }
    	nameArr = getAllActivatedFilter(invoker.getConfig(), nameArr);
        List<Object> filters = ExtensionLoader.getExtensionInstances(Filter.class, nameArr);
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; i --) {
                final Filter filter = (Filter) filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    @Override
					public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    @Override
					public RpcConfig getConfig() {
                        return invoker.getConfig();
                    }

                    @Override
					public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    @Override
					public RpcResult invoke(RpcRequest request) throws RpcException {
                        return filter.invoke(next, request);
                    }

                    @Override
					public void destroy() {
                        invoker.destroy();
                    }

                    @Override
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        return last;
    }
}