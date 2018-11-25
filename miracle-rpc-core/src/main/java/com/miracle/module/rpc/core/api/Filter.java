package com.miracle.module.rpc.core.api;

public interface Filter {
	/**
	 * do invoke filter.
	 * 
	 * <code>
	 * // before filter
     * Result result = invoker.invoke(invocation);
     * // after filter
     * return result;
     * </code>
	 */
	RpcResult invoke(Invoker<?> invoker, RpcRequest request) throws RpcException;
}
