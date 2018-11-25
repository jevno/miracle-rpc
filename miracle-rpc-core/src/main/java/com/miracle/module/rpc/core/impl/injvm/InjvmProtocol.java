package com.miracle.module.rpc.core.impl.injvm;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.AbstractProtocol;
import com.miracle.module.rpc.core.api.Exporter;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;

public class InjvmProtocol extends AbstractProtocol {
	
	private static InjvmProtocol INSTANCE = new InjvmProtocol();
	
	protected InjvmProtocol()
	{}
	
	public static InjvmProtocol getInjvmProtocol()
	{
		return INSTANCE;
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		return new InjvmExporter<T>(invoker, serviceKey(invoker.getConfig()), this.exporterMap);
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, RpcConfig config)
			throws RpcException {
		return new InjvmInvoker<T>(type, config, serviceKey(config), this.exporterMap);
	}

	public boolean isInjvmRefer(RpcConfig rpconfig)
	{
		boolean useLocal = rpconfig.getParameter(Constants.INJVM_KEY, false);
		if(useLocal)
		{
			String srvKey = serviceKey(rpconfig);
			if(this.exporterMap.containsKey(srvKey))
				return true;
		}
		return false;
	}
}
