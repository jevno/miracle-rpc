package com.miracle.module.rpc.core.impl.injvm;

import com.miracle.module.rpc.core.api.AbstractExporter;
import com.miracle.module.rpc.core.api.Exporter;
import com.miracle.module.rpc.core.api.Invoker;

import java.util.Map;


public class InjvmExporter<T> extends AbstractExporter<T> {

	private final String key;
	private final Map<String, Exporter<?>> exportedMaps;
	
	public InjvmExporter(Invoker<T> invoker, String key, Map<String, Exporter<?>> exportedMaps) {
		super(invoker);
		this.key = key;
		this.exportedMaps = exportedMaps;
		this.exportedMaps.put(key, this);
	}

	@Override
    public void unexport()
	{
		super.unexport();
		this.exportedMaps.remove(key);
	}
}
