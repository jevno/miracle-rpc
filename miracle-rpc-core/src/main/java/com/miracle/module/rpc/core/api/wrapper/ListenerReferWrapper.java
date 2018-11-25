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


import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.*;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * ListenerInvoker
 * 
 * @author william.liangf
 */
public class ListenerReferWrapper<T> implements Invoker<T> {

    private static final Logger logger = Logger.getLogger(ListenerReferWrapper.class);

    private final Invoker<T> invoker;
    
    private final List<ReferListener> listeners;

    public ListenerReferWrapper(Invoker<T> invoker, List<ReferListener> listeners){
        if (invoker == null) {
            throw new IllegalArgumentException("invoker == null");
        }
        this.invoker = invoker;
        this.listeners = listeners;
        if (listeners != null && listeners.size() > 0) {
            for (ReferListener listener : listeners) {
                if (listener != null) {
                    try {
                        listener.referred(invoker);
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                }
            }
        }
    }

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
        return invoker.invoke(request);
    }
    
    @Override
    public String toString() {
        return getInterface() + " -> " + getConfig()==null?" ":getConfig().toString();
    }

    @Override
    public void destroy() {
        try {
            invoker.destroy();
        } finally {
            if (listeners != null && listeners.size() > 0) {
                for (ReferListener listener : listeners) {
                    if (listener != null) {
                        try {
                            listener.destroyed(invoker);
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                }
            }
        }
    }

}