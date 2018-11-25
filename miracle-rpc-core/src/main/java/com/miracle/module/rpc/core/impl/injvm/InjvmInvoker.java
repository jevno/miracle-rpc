package com.miracle.module.rpc.core.impl.injvm;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.core.api.*;

import java.util.Map;

public class InjvmInvoker<T> extends AbstractInvoker<T> {

	private final String key;
	private final Map<String, Exporter<?>> exportedMaps;
	public InjvmInvoker(Class<T> type, RpcConfig config, String key, Map<String, Exporter<?>> exportedMaps) {
		super(type, config);
		this.key = key;
		this.exportedMaps = exportedMaps;
	}

	@Override
	protected RpcResult doInvoke(RpcRequest req) throws Throwable {
		Exporter<?> exporter = this.exportedMaps.get(key);
        if (exporter == null)  {
            throw new RpcException("Local Service [" + key + "] not found.");
        }
        return exporter.getInvoker().invoke(req);
	}

	@Override
    public boolean isAvailable()
	{
		InjvmExporter<?> exporter = (InjvmExporter<?>) exportedMaps.get(key);
    	if (exporter == null)  {
            return false;
        } else {
        	return super.isAvailable();
        }
	}
}
