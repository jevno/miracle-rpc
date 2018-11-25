package com.miracle.module.rpc.core.impl;

import com.miracle.module.rpc.core.api.AbstractExporter;
import com.miracle.module.rpc.core.api.Exporter;
import com.miracle.module.rpc.core.api.Invoker;

import java.util.Map;

public class DefaultExporter<T> extends AbstractExporter<T> {
	private final String key;
	private final Map<String, Exporter<?>> exportedMaps;
	
	public DefaultExporter(Invoker<T> invoker, String key, Map<String, Exporter<?>> exportedMaps)
	{
		super(invoker);
		this.key = key;
		this.exportedMaps = exportedMaps;
	}
	
	@Override
    public void unexport()
	{
		super.unexport();
		this.exportedMaps.remove(key);
	}
}
