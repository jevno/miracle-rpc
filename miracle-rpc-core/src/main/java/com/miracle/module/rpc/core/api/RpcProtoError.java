package com.miracle.module.rpc.core.api;

import java.io.Serializable;

public class RpcProtoError implements Serializable{

	private static final long serialVersionUID = 5793365273757043791L;
	
	private String exception;
	private String detail;
	
	public RpcProtoError(String exceptionName, String msg)
	{
		this.exception = exceptionName;
		this.detail = msg;
	}
	
	//支持fastJson 反序列化
	public RpcProtoError() {
	}
	
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	public String toString()
	{
		return "exception:" + exception + ", detail:" + detail;
	}
}
