package com.miracle.module.rpc.core.api;

import java.io.Serializable;

public abstract class RpcObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8797540724977578292L;
	
	protected int 				id;
	
	/**
	 * 序列化方式
	 */
	protected short 				 serializeType; /*client type, 0 for java using protostuff, 1 for other using hessian*/

	public short getSerializeType() {
		return serializeType;
	}

	public void setSerializeType(short serializeType) {
		this.serializeType = serializeType;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
