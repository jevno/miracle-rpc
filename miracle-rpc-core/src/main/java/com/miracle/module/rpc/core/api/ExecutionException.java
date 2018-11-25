package com.miracle.module.rpc.core.api;

public class ExecutionException extends Exception{

	private static final long serialVersionUID = -7227557838119804048L;
	private Object request;
	
	public ExecutionException(Object req, String msg)
	{
		super(msg);
		this.request = req;
	}
	
	public Object getRequest() {
		return request;
	}
}
