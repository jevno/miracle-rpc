package com.miracle.module.rpc.core.api;

public interface ReferListener {
	 void referred(Invoker<?> invoker) throws RpcException;
	 
	 void destroyed(Invoker<?> invoker);
}
