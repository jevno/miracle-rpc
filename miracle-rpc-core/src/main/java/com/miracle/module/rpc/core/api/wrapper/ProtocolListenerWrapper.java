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
import java.util.Collections;
import java.util.List;


import com.miracle.module.rpc.common.ExtensionLoader;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.*;


/**
 * ListenerProtocol
 * 
 * @author william.liangf
 */
public class ProtocolListenerWrapper implements Protocol {

    private final Protocol protocol;

    public ProtocolListenerWrapper(Protocol protocol){
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    @Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
    	List<ExporterListener> listeners = new ArrayList<ExporterListener>();
    	String listenerNames = invoker.getConfig().getParameter(Constants.LISTENER_KEY);
    	List<String> listenerClsList = new ArrayList<String>();
    	if(StringUtils.isNotEmpty(listenerNames))
    	{
    		String[] nameArr = listenerNames.split(",");
    		for(String clsName : nameArr)
    		{
    			listenerClsList.add(clsName);
    		}
    	}
    	if(!listenerClsList.contains(Constants.INJVM_EXPORTER_LISTENER))
    	{
    		listenerClsList.add(Constants.INJVM_EXPORTER_LISTENER);
    	}
    	
    	List<Object> extObjs = ExtensionLoader.getExtensionInstances(ExporterListener.class,
    			listenerClsList.toArray(new String[listenerClsList.size()]));
		if(extObjs != null && extObjs.size() > 0)
		{
			for(Object obj : extObjs)
			{
				listeners.add((ExporterListener)obj);
			}
		}
    	
        return new ListenerExporterWrapper<T>(protocol.export(invoker), Collections.unmodifiableList(listeners));
    }

	@Override
	public <T> Invoker<T> refer(Class<T> type, RpcConfig config) throws RpcException {
    	
    	List<ReferListener> listeners = new ArrayList<ReferListener>();
    	String listenerNames = config.getParameter(Constants.LISTENER_KEY);
    	if(StringUtils.isNotEmpty(listenerNames))
    	{
    		String[] nameArr = listenerNames.split(",");
    		List<Object> extObjs = ExtensionLoader.getExtensionInstances(ReferListener.class, nameArr);
    		if(extObjs != null && extObjs.size() > 0)
    		{
    			for(Object obj : extObjs)
    			{
    				listeners.add((ReferListener)obj);
    			}
    		}
    	}
    	
        return new ListenerReferWrapper<T>(protocol.refer(type, config), 
                Collections.unmodifiableList(listeners));
    }

    @Override
	public void destroy() {
    	ExtensionLoader.clearCachedExtensionInstances(ExporterListener.class);
    	ExtensionLoader.clearCachedExtensionInstances(ReferListener.class);
        protocol.destroy();
    }

}