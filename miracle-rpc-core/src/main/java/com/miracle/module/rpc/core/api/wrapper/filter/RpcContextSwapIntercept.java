package com.miracle.module.rpc.core.api.wrapper.filter;

public interface RpcContextSwapIntercept {

	public String getName();
	
	public void swapInAsConsumer();
	
	public void swapOutAsProvider();

	public void swapDoneAsConsumer();

	public void swapDoneAsProvider();
}
